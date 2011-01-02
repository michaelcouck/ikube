package ikube.index.handler.internet.crawler;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.database.mem.DataBaseMem;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	private IDataBase dataBase;
	private HttpClient httpClient;
	private IContentProvider<IndexableInternet> contentProvider;
	private List<Thread> threads;

	public PageHandler(List<Thread> threads) {
		this.dataBase = ApplicationContextManager.getBean(DataBaseMem.class);
		this.httpClient = new HttpClient();
		this.contentProvider = new InternetContentProvider();
		this.threads = threads;
	}

	public void run() {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.INDEXED, Boolean.FALSE);
		while (true) {
			List<Url> urls = dataBase.find(Url.class, parameters, 0, getIndexContext().getInternetBatchSize());
			if (urls.size() == 0) {
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
							logger.error("", e);
						}
					}
				} else {
					break;
				}
			}
			List<Url> list = new ArrayList<Url>();
			for (Url url : urls) {
				if (url.isIndexed()) {
					continue;
				}
				list.add(url);
				url.setIndexed(Boolean.TRUE);
				dataBase.merge(url);
			}
			for (Url url : list) {
				try {
					logger.info("Doing url : " + url);
					handle(url);
					handleChildren(url);
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
	}

	/**
	 * @See {@link IHandler#handle(Url)}
	 */
	public void handle(Url url) {
		try {
			IndexableInternet indexableInternet = getIndexableInternet();
			// Get the content from the url
			ByteOutputStream byteOutputStream = getContentFromUrl(httpClient, indexableInternet, url);
			if (byteOutputStream == null || byteOutputStream.size() == 0) {
				logger.warn("No content from url, perhaps exception : " + url);
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
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put(IConstants.HASH, hash);
			Url dbUrl = dataBase.find(Url.class, parameters, Boolean.TRUE);
			if (dbUrl != null) {
				logger.info("Duplicate data : " + dbUrl.getUrl());
				return;
			}
			url.setHash(hash);
			dataBase.merge(url);
			// Add the document to the index
			addDocumentToIndex(indexableInternet, url, parsedContent);
		} catch (Exception e) {
			logger.error("Exception visiting page : " + url, e);
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
	protected ByteOutputStream getContentFromUrl(HttpClient httpClient, IndexableInternet indexable, Url url) {
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
				if (get != null) {
					get.releaseConnection();
				}
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
			logger.error("Exception accessing url : " + url, e);
		} finally {
			url.setParsedContent(null);
			url.setRawContent(null);
			url.setTitle(null);
		}
	}

	protected String getUrlId(IndexableInternet indexableInternet, Url url) {
		StringBuilder builder = new StringBuilder();
		builder.append(indexableInternet.getName());
		builder.append(".");
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
	protected void extractLinksFromContent(IndexableInternet indexableInternet, Url baseUrl, InputStream inputStream) {
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

								Url dbUrl = dataBase.find(Url.class, id);
								if (dbUrl != null) {
									continue;
								}
								// Add the link to the database here
								dbUrl = new Url();
								dbUrl.setId(id);
								dbUrl.setUrl(strippedAnchorLink);
								dataBase.persist(dbUrl);
								// TODO - post this url on the url topic
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