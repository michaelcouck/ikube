package ikube.index.handler.internet.crawler;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.cluster.cache.ICache;
import ikube.index.IndexManager;
import ikube.index.content.ByteOutputStream;
import ikube.index.content.IContentProvider;
import ikube.index.content.InternetContentProvider;
import ikube.index.handler.internet.IndexableInternetHandler;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.index.parse.mime.MimeType;
import ikube.index.parse.mime.MimeTypes;
import ikube.index.parse.xml.XMLParser;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.UriUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.util.List;
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
@Deprecated
public class UrlPageHandler extends UrlHandler<Url> implements Runnable {

	private IClusterManager clusterManager;
	private IndexableInternetHandler handler;
	private IndexableInternet indexable;
	private transient final HttpClient httpClient;
	private transient final IContentProvider<IndexableInternet> contentProvider;

	private transient String urlToDo;
	private transient String urlDone;
	private transient String urlHash;
	private transient int batchSize;

	public UrlPageHandler(IClusterManager clusterManager, IndexableInternetHandler handler, IndexableInternet indexable, int batchSize,
			String id) {
		this.clusterManager = clusterManager;
		this.handler = handler;
		this.indexable = (IndexableInternet) SerializationUtilities.clone(indexable);
		this.httpClient = new HttpClient();
		this.contentProvider = new InternetContentProvider();
		this.urlToDo = IConstants.URL + id;
		this.urlDone = IConstants.URL_DONE + id;
		this.urlHash = IConstants.URL_ID + id;
		this.batchSize = batchSize;
	}

	private ICache.IAction<Url> action = new ICache.IAction<Url>() {
		@Override
		public void execute(Url url) {
			try {
				if (url != null) {
					getClusterManager().remove(urlToDo, url.getId());
					getClusterManager().set(urlDone, url.getId(), url);
				}
			} catch (Exception e) {
				LOGGER.error("Exception adding the url to the cache : " + url, e);
			}
		}
	};

	@Override
	public void run() {
		while (true) {
			List<Url> urls = clusterManager.get(Url.class, urlToDo, null, action, batchSize);
			if (urls.isEmpty()) {
				// Check if there are any other threads still working
				// other than this thread of course
				if (handler.isCrawling(null)) {
					synchronized (this) {
						try {
							wait(1000);
						} catch (Exception e) {
							LOGGER.error("Exception waiting for more resources to crawl : ", e);
						}
					}
				} else {
					break;
				}
			}
			LOGGER.info("Doing urls : " + urls.size());
			if (urls.size() > 0) {
				LOGGER.info("Last url : " + urls.get(urls.size()));
			}
			for (Url url : urls) {
				try {
					if (url == null || url.getUrl() == null) {
						continue;
					}
					handle(url);
					handleChildren(url);
					url.setParsedContent(null);
					url.setRawContent(null);
					url.setTitle(null);
					url.setUrl(null);
					url.setContentType(null);
					getClusterManager().set(urlDone, url.getId(), url);
				} catch (Exception e) {
					LOGGER.error("Exception doing url : " + url, e);
				}
			}
		}
	}

	/**
	 * @See {@link IUrlHandler#handle(Url)}
	 */
	@Override
	public void handle(final Url url) {
		try {
			// Get the content from the url
			ByteOutputStream byteOutputStream = getContentFromUrl(httpClient, indexable, url);
			if (byteOutputStream == null || byteOutputStream.size() == 0) {
				LOGGER.warn("No content from url, perhaps exception : " + url);
				return;
			}
			InputStream inputStream = new ByteArrayInputStream(byteOutputStream.getBytes());
			// Extract the links from the url if any
			extractLinksFromContent(indexable, url, inputStream);

			// Parse the content from the url
			String parsedContent = getParsedContent(url, byteOutputStream);
			if (parsedContent == null) {
				return;
			}
			long hash = HashUtilities.hash(parsedContent);
			url.setHash(hash);
			if (clusterManager.get(urlHash, url.getHash()) != null) {
				LOGGER.info("Duplicate data : " + url.getUrl());
				return;
			}
			clusterManager.set(urlHash, url.getHash(), url);
			// Add the document to the index
			addDocumentToIndex(indexable, url, parsedContent);
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
			LOGGER.error("Exception getting the content from the url : " + url, e);
		} finally {
			try {
				if (get != null) {
					get.releaseConnection();
				}
			} catch (Exception e) {
				LOGGER.error("Exception releasing the connection to the url : " + url, e);
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
			handler.addDocument(null, indexable, document);
			// getIndexContext().getIndex().getIndexWriter().addDocument(document);
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
				if (tag.getName().equals(HTMLElementName.A) && StartTag.class.isAssignableFrom(tag.getClass())) {
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

							if (clusterManager.get(urlToDo, id) != null || clusterManager.get(urlDone, id) != null) {
								continue;
							}
							Url url = new Url();
							url.setId(id);
							// Add the link to the database here
							url.setUrl(strippedAnchorLink);
							clusterManager.set(urlToDo, url.getId(), url);
							// setUrl(dbUrl);
						} catch (Exception e) {
							LOGGER.error("Exception extracting link : " + tag, e);
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("General exception extracting the links from : ", e);
		}
	}

	protected IClusterManager getClusterManager() {
		return this.clusterManager;
	}

}