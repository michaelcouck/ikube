package ikube.index.handler.internet.crawler;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.index.content.ByteOutputStream;
import ikube.index.content.IContentProvider;
import ikube.index.content.InternetContentProvider;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.index.parse.mime.MimeType;
import ikube.index.parse.mime.MimeTypes;
import ikube.index.parse.xml.XMLParser;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.text.html.HTML;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Element;
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
 * @since 25.09.10
 * @version 01.00
 */
public class PageHandler extends Handler<Url> implements Runnable {

	public static final Set<Url> IN_SET = new TreeSet<Url>(new Comparator<Url>() {
		@Override
		public int compare(final Url objectOne, final Url objectTwo) {
			long id1 = objectOne.getId();
			long id2 = objectTwo.getId();
			return id1 < id2 ? -1 : id1 == id2 ? 0 : 1;
		}
	});
	public static final Set<Url> OUT_SET = new TreeSet<Url>(new Comparator<Url>() {
		@Override
		public int compare(final Url objectOne, final Url objectTwo) {
			long id1 = objectOne.getId();
			long id2 = objectTwo.getId();
			return id1 < id2 ? -1 : id1 == id2 ? 0 : 1;
		}
	});
	public static final Set<Url> HASH_SET = new TreeSet<Url>(new Comparator<Url>() {
		@Override
		public int compare(final Url objectOne, final Url objectTwo) {
			long hash1 = objectOne.getHash();
			long hash2 = objectTwo.getHash();
			return hash1 < hash2 ? -1 : hash1 == hash2 ? 0 : 1;
		}
	});

	private transient final HttpClient httpClient;
	private transient final IContentProvider<IndexableInternet> contentProvider;
	private transient final List<Thread> threads;

	public PageHandler(final List<Thread> threads) {
		this.httpClient = new HttpClient();
		this.contentProvider = new InternetContentProvider();
		this.threads = threads;
		IN_SET.clear();
		OUT_SET.clear();
		HASH_SET.clear();
	}

	@Override
	public void run() {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.INDEXED, Boolean.FALSE);
		while (true) {
			List<Url> urls = getBatch(getIndexContext().getInternetBatchSize());
			if (urls.isEmpty()) {
				// Check if there are any other threads still working
				// other than this thread of course
				int threadsRunnable = 0;
				for (Thread thread : threads) {
					if (thread.getState().equals(State.RUNNABLE)) {
						threadsRunnable++;
					}
				}
				if (threadsRunnable >= 2) {
					synchronized (this) {
						try {
							wait(1000);
						} catch (Exception e) {
							LOGGER.error("", e);
						}
					}
				} else {
					break;
				}
			}
			for (Url url : urls) {
				try {
					// LOGGER.info("Doing url : " + url);
					handle(url);
					handleChildren(url);
					url.setParsedContent(null);
					url.setRawContent(null);
					url.setTitle(null);
					url.setUrl(null);
					url.setContentType(null);
				} catch (Exception e) {
					LOGGER.error("", e);
				}
			}
		}
	}

	protected static synchronized List<Url> getBatch(final int batchSize) {
		try {
			List<Url> batch = new ArrayList<Url>();
			for (Url url : IN_SET) {
				batch.add(url);
				if (batch.size() >= batchSize) {
					break;
				}
			}
			for (Url url : batch) {
				IN_SET.remove(url);
				OUT_SET.add(url);
			}
			return batch;
		} finally {
			PageHandler.class.notifyAll();
		}
	}

	/**
	 * @See {@link IHandler#handle(Url)}
	 */
	@Override
	public void handle(final Url url) {
		try {
			IndexableInternet indexableInternet = getIndexableInternet();
			// Get the content from the url
			ByteOutputStream byteOutputStream = getContentFromUrl(httpClient, indexableInternet, url);
			if (byteOutputStream == null || byteOutputStream.size() == 0) {
				LOGGER.warn("No content from url, perhaps exception : " + url);
				return;
			}
			InputStream inputStream = new ByteArrayInputStream(byteOutputStream.getBytes());
			// Extract the links from the url if any
			extractLinksFromContent(indexableInternet, url, inputStream);

			// Parse the content from the url
			String parsedContent = getParsedContent(url, byteOutputStream);
			if (parsedContent == null) {
				return;
			}
			long hash = HashUtilities.hash(parsedContent);
			url.setHash(hash);
			if (HASH_SET.contains(url)) {
				LOGGER.info("Duplicate data : " + url.getUrl());
				return;
			}
			HASH_SET.add(url);
			// Add the document to the index
			addDocumentToIndex(indexableInternet, url, parsedContent);
		} catch (Exception e) {
			LOGGER.error("Exception visiting page : " + url, e);
		}
	}

	/**
	 * Gets the raw data from the url.
	 * 
	 * @param indexable
	 *            the indexable to set the transient data in
	 * @param url
	 *            the url to get the data from
	 * @return the raw data from the url
	 */
	protected ByteOutputStream getContentFromUrl(final HttpClient httpClient, final IndexableInternet indexable, final Url url) {
		GetMethod get = null;
		ByteOutputStream byteOutputStream = null;
		try {
			get = new GetMethod(url.getUrl());
			httpClient.executeMethod(get);
			InputStream responseInputStream = get.getResponseBodyAsStream();
			indexable.setCurrentInputStream(responseInputStream);
			byteOutputStream = new ByteOutputStream();
			contentProvider.getContent(indexable, byteOutputStream);
			url.setRawContent(byteOutputStream.getBytes());
			return byteOutputStream;
		} catch (Exception e) {
			LOGGER.error("", e);
		} finally {
			try {
				if (get != null) {
					get.releaseConnection();
				}
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}
		return byteOutputStream;
	}

	/**
	 * Parses the content from the input stream into a string. The content can be anything, rich text, xml, etc.
	 * 
	 * @param url
	 *            the url where the data is
	 * @param byteOutputStream
	 *            the output stream of data from the url
	 * @return the parsed content
	 */
	protected String getParsedContent(final Url url, final ByteOutputStream byteOutputStream) {
		try {
			String contentType = URI.create(url.getUrl()).toURL().getFile();
			// The actual byte buffer of data
			byte[] buffer = byteOutputStream.getBytes();
			// The first few bytes so we can guess the content type
			byte[] bytes = new byte[Math.min(buffer.length, 1024)];
			System.arraycopy(buffer, 0, bytes, 0, bytes.length);
			IParser parser = ParserProvider.getParser(contentType, bytes);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer, 0, byteOutputStream.getCount());
			OutputStream outputStream = null;
			try {
				outputStream = parser.parse(byteArrayInputStream, new ByteArrayOutputStream());
			} catch (Exception e) {
				// If this is an XML exception then try the HTML parser
				if (XMLParser.class.isAssignableFrom(parser.getClass())) {
					contentType = "html";
					parser = ParserProvider.getParser(contentType, bytes);
					outputStream = parser.parse(byteArrayInputStream, new ByteArrayOutputStream());
				} else {
					throw e;
				}
			}
			url.setContentType(contentType);
			return outputStream.toString();
		} catch (Exception e) {
			LOGGER.error("Exception accessing url : " + url, e);
		}
		return null;
	}

	/**
	 * Adds the document to the index with all the defined fields.
	 * 
	 * @param indexable
	 * @param url
	 * @param parsedContent
	 */
	protected void addDocumentToIndex(final IndexableInternet indexable, final Url url, final String parsedContent) {
		try {
			String id = getUrlId(indexable, url);

			Document document = new Document();
			Store store = indexable.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;

			// Add the title field
			MimeType mimeType = MimeTypes.getMimeType(url.getContentType(), url.getRawContent());
			if (mimeType != null && mimeType.getPrimaryType().contains(HTMLElementName.HTML)) {
				InputStream inputStream = new ByteArrayInputStream(url.getRawContent());
				Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
				Source source = new Source(reader);
				Element titleElement = source.getNextElement(0, HTMLElementName.TITLE);
				if (titleElement != null) {
					String title = titleElement.getContent().toString();
					IndexManager.addStringField(indexable.getTitleFieldName(), title, document, store, analyzed, termVector);
				}
			} else {
				// Add the url as the title
				IndexManager.addStringField(indexable.getTitleFieldName(), url.getUrl(), document, store, analyzed, termVector);
			}
			// Add the id field
			IndexManager.addStringField(indexable.getIdFieldName(), id, document, Store.YES, Index.ANALYZED, TermVector.YES);
			// Add the contents field
			IndexManager.addStringField(indexable.getContentFieldName(), parsedContent, document, store, analyzed, termVector);
			getIndexContext().getIndex().getIndexWriter().addDocument(document);
		} catch (Exception e) {
			LOGGER.error("Exception accessing url : " + url, e);
		} finally {
			url.setParsedContent(null);
			url.setRawContent(null);
			url.setTitle(null);
		}
	}

	protected String getUrlId(final IndexableInternet indexableInternet, final Url url) {
		StringBuilder builder = new StringBuilder();
		builder.append(url.getUrl());
		return builder.toString();
	}

	/**
	 * Extracts all the links from the content and sets them in the cluster wide cache. The cache is persistence backed so any overflow then
	 * goes to a local object oriented database on each server.
	 * 
	 * @param indexableInternet
	 *            the indexable that is being crawled
	 * @param baseUrl
	 *            the base url that the link was found in
	 * @param inputStream
	 *            the input stream of the data from the base url, i.e. the html
	 */
	protected void extractLinksFromContent(final IndexableInternet indexableInternet, final Url baseUrl, final InputStream inputStream) {
		try {
			Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
			Source source = new Source(reader);
			List<Tag> tags = source.getAllTags();
			URI baseUri = new URI(baseUrl.getUrl());
			String baseHost = indexableInternet.getUri().getHost();
			Pattern pattern = null;
			if (indexableInternet.getExcludedPattern() != null) {
				pattern = Pattern.compile(indexableInternet.getExcludedPattern());
			}
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
								if (UriUtilities.isExcluded(link.trim().toLowerCase())) {
									continue;
								}
								String resolvedLink = UriUtilities.resolve(baseUri, link);
								if (!UriUtilities.isInternetProtocol(resolvedLink)) {
									continue;
								}
								if (!resolvedLink.contains(baseHost)) {
									continue;
								}
								if (pattern != null && pattern.matcher(resolvedLink).matches()) {
									continue;
								}
								String replacement = resolvedLink.contains("?") ? "?" : "";
								String strippedSessionLink = UriUtilities.stripJSessionId(resolvedLink, replacement);
								String strippedAnchorLink = UriUtilities.stripAnchor(strippedSessionLink, "");
								Long id = HashUtilities.hash(strippedAnchorLink);
								Url dbUrl = new Url();
								dbUrl.setId(id);
								if (exists(dbUrl)) {
									continue;
								}
								// Add the link to the database here

								dbUrl.setUrl(strippedAnchorLink);
								setUrl(dbUrl);
							} catch (Exception e) {
								LOGGER.error("Exception extracting link : " + tag, e);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	protected static synchronized boolean exists(final Url url) {
		try {
			return IN_SET.contains(url) || OUT_SET.contains(url);
		} finally {
			PageHandler.class.notifyAll();
		}
	}

	protected static synchronized void setUrl(final Url url) {
		try {
			IN_SET.add(url);
		} finally {
			PageHandler.class.notifyAll();
		}
	}

}