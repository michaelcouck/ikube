package ikube.index.visitor.internet;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.index.visitor.IndexableVisitor;
import ikube.listener.ListenerManager;
import ikube.model.Event;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.UriUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.Thread.State;
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableInternetVisitor<T> extends IndexableVisitor<IndexableInternet> {

	/** Accepted protocols. */
	private final Pattern PROTOCOL_PATTERN = Pattern.compile("(http).*|(www).*|(https).*|(ftp).*");
	/** The pattern regular expression to match a url. */
	private final Pattern EXCLUDED_PATTERN = Pattern.compile(".*news.*|.*javascript.*|.*mailto.*|.*plugintest.*|.*skype.*");

	private IDataBase dataBase;
	private int threads;

	@Override
	public void visit(final IndexableInternet indexable) {
		final List<Thread> synchronizedThreads = Collections.synchronizedList(new ArrayList<Thread>());
		try {
			// The start url
			String urlString = indexable.getUrl();
			indexable.setCurrentUrl(urlString);

			Url url = new Url();
			url.setUrl(urlString);
			url.setName(indexable.getName());
			url.setIndexed(Boolean.FALSE);

			Event event = new Event();
			event.setConsumed(Boolean.FALSE);
			event.setIndexContext(getIndexContext());
			event.setObject(url);
			event.setTimestamp(new Timestamp(System.currentTimeMillis()));
			event.setType(Event.LINK);
			ListenerManager.fireEvent(event);

			Thread thread = null;
			for (int i = 0; i < threads; i++) {
				thread = new Thread(new Runnable() {
					public void run() {
						HttpClient httpClient = new HttpClient();
						while (true) {
							Url url = getNextUrl(indexable, synchronizedThreads);
							if (url == null) {
								return;
							}
							visitUrl(indexable, url, httpClient);
						}
					}
				});
				synchronizedThreads.add(thread);
				thread.start();
			}
			thread.join();
		} catch (Exception e) {
			logger.error("Exception reading the url : " + indexable.getUrl(), e);
		}
		logger.debug("Finished indexing : ");
	}

	protected synchronized Url getNextUrl(final IndexableInternet indexable, final List<Thread> synchronizedThreads) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.NAME, indexable.getName());
		parameters.put(IConstants.INDEXED, Boolean.FALSE);
		Url url = dataBase.find(Url.class, parameters, Boolean.FALSE);
		if (url == null) {
			for (Thread thread : synchronizedThreads) {
				logger.debug("Thread : " + thread + ", " + Thread.currentThread());
				if (thread.equals(Thread.currentThread())) {
					continue;
				}
				// Check that there is one thread still active
				if (thread.getState().equals(State.RUNNABLE)) {
					logger.debug("Going into wait : " + Thread.currentThread());
					try {
						wait(1000);
					} catch (InterruptedException e) {
						logger.error("", e);
					}
					return getNextUrl(indexable, synchronizedThreads);
				}
			}
		}
		if (url != null) {
			url.setIndexed(Boolean.TRUE);
			dataBase.merge(url);
		}
		notifyAll();
		return url;
	}

	protected void visitUrl(IndexableInternet indexable, Url url, HttpClient httpClient) {
		logger.debug("Doing url : " + url.getUrl() + ", " + Thread.currentThread());
		GetMethod get = null;
		try {
			get = new GetMethod(url.getUrl());
			httpClient.executeMethod(get);
			InputStream inputStream = get.getResponseBodyAsStream();
			String contentType = URI.create(url.getUrl()).toURL().getFile();
			// logger.debug("Content type : " + contentType);

			ByteArrayOutputStream byteArrayOutputStream = FileUtilities.getContents(inputStream, Integer.MAX_VALUE);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			// ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(get.getResponseBodyAsString().getBytes());

			byte[] bytes = new byte[1024];
			byteArrayInputStream.mark(bytes.length);
			byteArrayInputStream.read(bytes);
			byteArrayInputStream.reset();

			IParser parser = ParserProvider.getParser(contentType, bytes);
			OutputStream outputStream = parser.parse(byteArrayInputStream);
			// TODO - Add the title field
			// TODO - Add the contents field
			String fieldContents = outputStream.toString();
			// logger.debug("Parsed : " + fieldContents);

			Document document = new Document();
			Store store = indexable.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;

			addStringField(indexable.getName(), fieldContents, document, store, analyzed, termVector);

			getIndexContext().getIndexWriter().addDocument(document);

			byteArrayInputStream.reset();
			extractLinks(indexable, url, byteArrayInputStream);
		} catch (Exception e) {
			logger.error("Exception accessing url : " + url, e);
		} finally {
			try {
				get.releaseConnection();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	protected void extractLinks(IndexableInternet indexable, Url baseUrl, InputStream inputStream) throws Exception {
		// Extract the links
		Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
		Source source = new Source(reader);
		List<Tag> tags = source.getAllTags();
		// logger.debug("Tags : " + tags);
		URI baseUri = new URI(baseUrl.getUrl());
		for (Tag tag : tags) {
			if (tag.getName().equals(HTMLElementName.A)) {
				// logger.debug("Tag : " + tag);
				if (StartTag.class.isAssignableFrom(tag.getClass())) {
					Attribute attribute = ((StartTag) tag).getAttributes().get("href");
					if (attribute != null) {
						String link = attribute.getValue();
						if (link == null) {
							continue;
						}
						Url newUrl = new Url();
						if (PROTOCOL_PATTERN.matcher(link).matches()) {
							String baseHost = indexable.getUrl();
							if (!link.contains(baseHost)) {
								continue;
							}
							newUrl.setUrl(link);
						} else {
							String lowerCaseLink = link.toLowerCase();
							if (EXCLUDED_PATTERN.matcher(lowerCaseLink).matches()) {
								continue;
							}
							URI uri = UriUtilities.resolve(baseUri, link);
							String baseHost = indexable.getUri().getHost();
							String uriString = uri.toString();
							if (!uriString.contains(baseHost)) {
								continue;
							}
							// TODO - strip the session id off the link
							newUrl.setUrl(uri.toString());
						}
						newUrl.setName(indexable.getName());
						newUrl.setIndexed(Boolean.FALSE);
						// logger.debug("Found url : " + newUrl.getUrl());
						Event event = new Event();
						event.setObject(newUrl);
						event.setTimestamp(new Timestamp(System.currentTimeMillis()));
						event.setType(Event.LINK);
						ListenerManager.fireEvent(event);
					}
				}
			}
		}
	}

	public void setDataBase(IDataBase dataBase) {
		this.dataBase = dataBase;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

}
