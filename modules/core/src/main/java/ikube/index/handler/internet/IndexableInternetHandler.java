package ikube.index.handler.internet;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.index.content.ByteOutputStream;
import ikube.index.content.IContentProvider;
import ikube.index.content.InternetContentProvider;
import ikube.index.handler.Handler;
import ikube.index.handler.IHandler;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.UriUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.Thread.State;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.html.HTML;

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
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableInternetHandler extends Handler {

	private IContentProvider<IndexableInternet> contentProvider;

	public IndexableInternetHandler(IHandler<Indexable<?>> previous) {
		super(previous);
		this.contentProvider = new InternetContentProvider();
	}

	@Override
	public List<Thread> handle(IndexContext indexContext, Indexable<?> indexable) throws Exception {
		if (IndexableInternet.class.isAssignableFrom(indexable.getClass())) {
			return handleInternet(indexContext, (IndexableInternet) indexable);
		}
		return new ArrayList<Thread>();
	}

	protected List<Thread> handleInternet(final IndexContext indexContext, final IndexableInternet internetIndexable) {
		final List<Thread> threads = new ArrayList<Thread>();
		// The start url
		seedUrl(internetIndexable);
		for (int i = 0; i < getThreads(); i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					HttpClient httpClient = new HttpClient();
					while (true) {
						List<Url> urls = getNextUrls(internetIndexable, threads);
						if (urls.size() == 0) {
							return;
						}
						for (Url url : urls) {
							handleUrl(indexContext, internetIndexable, url, httpClient);
						}
					}
				}
			}, this.getClass().getSimpleName() + "." + i);
			threads.add(thread);
			thread.start();
		}
		return threads;
	}

	protected synchronized List<Url> getNextUrls(final IndexableInternet indexable, final List<Thread> threads) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.NAME, indexable.getName());
		parameters.put(IConstants.INDEXED, Boolean.FALSE);
		List<Url> urls = getDataBase().find(Url.class, parameters, 0, 100);
		if (urls.size() == 0) {
			for (Thread thread : threads) {
				// logger.debug(Logging.getString("Thread : ", thread, ", ", Thread.currentThread()));
				if (thread.equals(Thread.currentThread())) {
					continue;
				}
				// Check that there is one thread still active
				if (thread.getState().equals(State.RUNNABLE)) {
					logger.debug(Logging.getString("Going into wait : ", Thread.currentThread()));
					try {
						wait(100);
					} catch (InterruptedException e) {
						logger.error("", e);
					}
					return getNextUrls(indexable, threads);
				}
			}
		}
		if (urls.size() > 0) {
			for (Url url : urls) {
				url.setIndexed(Boolean.TRUE);
				getDataBase().merge(url);
			}
		}
		notifyAll();
		return urls;
	}

	protected void handleUrl(IndexContext indexContext, IndexableInternet indexable, Url url, HttpClient httpClient) {
		logger.debug("Doing url : " + url.getUrl() + ", " + Thread.currentThread());
		GetMethod get = null;
		ByteOutputStream byteOutputStream = null;
		try {
			get = new GetMethod(url.getUrl());
			httpClient.executeMethod(get);
			InputStream inputStream = get.getResponseBodyAsStream();

			indexable.setCurrentInputStream(inputStream);

			String contentType = URI.create(url.getUrl()).toURL().getFile();

			byteOutputStream = new ByteOutputStream();
			contentProvider.getContent(indexable, byteOutputStream);

			byte[] buffer = byteOutputStream.getBytes();
			int length = Math.min(buffer.length, 1024);
			byte[] bytes = new byte[length];

			System.arraycopy(buffer, 0, bytes, 0, bytes.length);

			inputStream = new ByteArrayInputStream(buffer, 0, byteOutputStream.getCount());

			IParser parser = ParserProvider.getParser(contentType, bytes);
			OutputStream outputStream = parser.parse(inputStream, new ByteArrayOutputStream());
			// TODO - Add the title field
			// TODO - Add the contents field
			String fieldContents = outputStream.toString();

			Map<String, Object> parameters = new HashMap<String, Object>();
			Long hash = HashUtilities.hash(fieldContents);
			parameters.put(IConstants.HASH, hash);
			Url duplicate = getDataBase().find(Url.class, parameters, Boolean.FALSE);
			if (duplicate != null) {
				logger.debug("Found duplicate data : " + duplicate);
				return;
			}

			url.setHash(hash);
			getDataBase().merge(url);

			Document document = new Document();
			Store store = indexable.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;

			setIdField(indexable, document);
			IndexManager.addStringField(indexable.getName(), fieldContents, document, store, analyzed, termVector);

			indexContext.getIndexWriter().addDocument(document);

			inputStream.reset();
			extractLinks(indexable, url, inputStream);
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

	protected void setIdField(IndexableInternet indexableInternet, Document document) throws Exception {
		StringBuilder builder = new StringBuilder();
		builder.append(indexableInternet.getName());
		builder.append(".");
		builder.append(indexableInternet.getCurrentUrl());
		String id = builder.toString();
		IndexManager.addStringField(IConstants.ID, id, document, Store.YES, Index.ANALYZED, TermVector.YES);
	}

	protected void extractLinks(IndexableInternet indexable, Url baseUrl, InputStream inputStream) throws Exception {
		// Extract the links
		Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
		Source source = new Source(reader);
		List<Tag> tags = source.getAllTags();
		URI baseUri = new URI(baseUrl.getUrl());
		String baseHost = indexable.getUri().getHost();
		for (Tag tag : tags) {
			if (tag.getName().equals(HTMLElementName.A)) {
				if (StartTag.class.isAssignableFrom(tag.getClass())) {
					Attribute attribute = ((StartTag) tag).getAttributes().get(HTML.Attribute.HREF.toString());
					if (attribute != null) {
						try {
							String link = attribute.getValue();
							if (link == null) {
								continue;
							}
							if (UriUtilities.isExcluded(link.toLowerCase())) {
								continue;
							}
							URI uri = UriUtilities.resolve(baseUri, link);
							String resolvedLink = uri.toString();
							if (!UriUtilities.isInternetProtocol(resolvedLink)) {
								continue;
							}
							if (!resolvedLink.contains(baseHost)) {
								continue;
							}
							String replacement = resolvedLink.contains("?") ? "?" : "";
							String strippedSessionLink = UriUtilities.stripJSessionId(resolvedLink, replacement);
							String strippedAnchorLink = UriUtilities.stripAnchor(strippedSessionLink, "");
							Url newUrl = new Url();
							newUrl.setUrl(strippedAnchorLink);
							newUrl.setName(indexable.getName());
							newUrl.setIndexed(Boolean.FALSE);
							// Persist the url in the database
							persistUrl(newUrl);
						} catch (Exception e) {
							logger.error("Exception extracting link : " + tag, e);
						}
					}
				}
			}
		}
	}

	protected void seedUrl(final IndexableInternet indexable) {
		String urlString = indexable.getUrl();
		indexable.setCurrentUrl(urlString);

		Url url = new Url();
		url.setUrl(urlString);
		url.setName(indexable.getName());
		url.setIndexed(Boolean.FALSE);

		persistUrl(url);
	}

	protected void persistUrl(Url url) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.URL, url.getUrl());
		parameters.put(IConstants.NAME, url.getName());
		Url dbUrl = getDataBase().find(Url.class, parameters, Boolean.TRUE);
		// logger.debug("Event : " + event + ", " + dbUrl + ", " + url);
		if (dbUrl == null) {
			// logger.debug("Persisting : " + url);
			getDataBase().persist(url);
		}
	}

}