package ikube.index.handler.internet;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.index.IndexManager;
import ikube.index.content.ByteOutputStream;
import ikube.index.content.IContentProvider;
import ikube.index.content.InternetContentProvider;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.index.parse.mime.MimeType;
import ikube.index.parse.mime.MimeTypes;
import ikube.index.parse.xml.XMLParser;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
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
import java.util.List;

import javax.swing.text.html.HTML;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableInternetCrawler implements Runnable {

	private Logger logger;
	private long waitTime = 3000;
	private List<Thread> threads;
	private IndexContext indexContext;
	private IndexableInternet indexableInternet;
	private IContentProvider<IndexableInternet> contentProvider;
	private HttpClient httpClient;

	public IndexableInternetCrawler(IndexContext indexContext, IndexableInternet indexableInternet, List<Thread> threads) {
		this.logger = Logger.getLogger(this.getClass());
		this.threads = threads;
		this.indexContext = indexContext;
		this.indexableInternet = indexableInternet;
		this.contentProvider = new InternetContentProvider();
		this.httpClient = new HttpClient();
	}

	/**
	 * This method iterates over a batch if urls. Urls are stored in the distributed map in the cluster. As they are accessed by this class
	 * the indexed flag is set to true. When there are no threads running and there are no urls in the map that are still not indexed then
	 * this thread will return and die.
	 */
	@Override
	public void run() {
		while (true) {
			List<Url> urls = getBatch(threads);
			if (urls.size() == 0) {
				// If we have no more urls it means that
				// there are not more in the cache and all the
				// other threads are waiting too, so we die
				return;
			}
			for (Url url : urls) {
				logger.debug("Doing url : " + url.getUrl() + ", " + Thread.currentThread().hashCode());
				// Get the content from the url
				ByteOutputStream byteOutputStream = getContentFromUrl(indexableInternet, url);
				// Parse the content from the url
				String parsedContent = getParsedContent(url, byteOutputStream);
				// Add the document to the index
				addDocumentToIndex(indexableInternet, url, parsedContent);
				InputStream inputStream = new ByteArrayInputStream(byteOutputStream.getBytes());
				// Extract the links from the url if any
				extractLinksFromContent(indexableInternet, url, inputStream);
				try {
					Thread.sleep(indexContext.getThrottle());
				} catch (Exception e) {
					logger.error("Sleep interrupted : ", e);
				}
			}
		}
	}

	/**
	 * This method gets a batch of urls to index. This thread has access to all the threads that are crawling the site. If there are no more
	 * urls in the map that are not indexed then this thread will wait for any one of the running threads to add urls to the map.
	 * 
	 * @param threads
	 *            the threads that are indexing the site
	 * @return a list of urls to crawl which could be empty, in which case the site is completely crawled
	 */
	protected synchronized List<Url> getBatch(List<Thread> threads) {
		try {
			IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
			List<Url> urls = clusterManager.getBatch(indexContext.getInternetBatchSize());
			if (urls.size() == 0) {
				for (Thread thread : threads) {
					if (thread.equals(Thread.currentThread())) {
						continue;
					}
					if (thread.getState().equals(State.RUNNABLE)) {
						try {
							wait(waitTime);
						} catch (InterruptedException e) {
							logger.error("", e);
						}
						return getBatch(threads);
					}
				}
				// TODO - we need to check to see if there are any other servers
				// running the internet handler, in that case we need to wait for these
				// servers to run out of threads
			}
			return urls;
		} finally {
			notifyAll();
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
	protected ByteOutputStream getContentFromUrl(IndexableInternet indexable, Url url) {
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
			logger.error("", e);
		} finally {
			try {
				get.releaseConnection();
			} catch (Exception e) {
				logger.error("", e);
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
	protected String getParsedContent(Url url, ByteOutputStream byteOutputStream) {
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
			logger.error("", e);
		} finally {
			// Nothing
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
	protected void addDocumentToIndex(IndexableInternet indexable, Url url, String parsedContent) {
		try {
			Long hash = HashUtilities.hash(parsedContent);
			url.setHash(hash);

			IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
			Url duplicate = clusterManager.get(Url.class, "hash = " + hash);
			if (duplicate != null) {
				logger.debug(Logging.getString("Found duplicate data : ", duplicate, ", url : ", url));
				return;
			}

			String id = getUrlId();

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
			indexContext.getIndex().getIndexWriter().addDocument(document);
		} catch (Exception e) {
			logger.error("Exception accessing url : " + url, e);
		} finally {
			url.setParsedContent(null);
			url.setRawContent(null);
			url.setTitle(null);
		}
	}

	protected String getUrlId() {
		StringBuilder builder = new StringBuilder();
		builder.append(indexableInternet.getName());
		builder.append(".");
		builder.append(indexableInternet.getCurrentUrl());
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
	protected void extractLinksFromContent(IndexableInternet indexableInternet, Url baseUrl, InputStream inputStream) {
		try {
			Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
			Source source = new Source(reader);
			List<Tag> tags = source.getAllTags();
			URI baseUri = new URI(baseUrl.getUrl());
			String baseHost = indexableInternet.getUri().getHost();
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
								String replacement = resolvedLink.contains("?") ? "?" : "";
								String strippedSessionLink = UriUtilities.stripJSessionId(resolvedLink, replacement);
								String strippedAnchorLink = UriUtilities.stripAnchor(strippedSessionLink, "");
								Long id = HashUtilities.hash(strippedAnchorLink);

								IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
								if (clusterManager.get(Url.class, id) == null) {
									Url newUrl = new Url();
									newUrl.setId(id);
									newUrl.setUrl(strippedAnchorLink);
									newUrl.setIndexed(Boolean.FALSE);
									clusterManager.set(Url.class, id, newUrl);
								}
							} catch (Exception e) {
								logger.error("Exception extracting link : " + tag, e);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}