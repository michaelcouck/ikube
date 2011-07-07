package ikube.index.handler.internet;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.index.content.ByteOutputStream;
import ikube.index.content.IContentProvider;
import ikube.index.content.InternetContentProvider;
import ikube.index.handler.IndexableHandler;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.index.parse.mime.MimeType;
import ikube.index.parse.mime.MimeTypes;
import ikube.index.parse.xml.XMLParser;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.UriUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.swing.text.html.HTML;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;

/**
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableInternetHandler extends IndexableHandler<IndexableInternet> {

	/** TODO Remove these fields and put them in the IndexableInternet. */
	private Pattern excludedPattern;
	private Map<String, Object> notIndexedParameters;
	private Cache cache = CacheManager.create().getCache("UrlCache");
	private Cache newCache = CacheManager.create().getCache("NewUrlCache");
	private ThreadLocal<Map<String, Object>> localUrlIdParameters = new ThreadLocal<Map<String, Object>>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Thread> handle(final IndexContext<?> indexContext, final IndexableInternet indexable) throws Exception {
		cache.removeAll();
		newCache.removeAll();
		if (indexable.getExcludedPattern() != null) {
			excludedPattern = Pattern.compile(indexable.getExcludedPattern());
		}
		final IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		notIndexedParameters = new HashMap<String, Object>();
		notIndexedParameters.put(IConstants.INDEXED, Boolean.FALSE);
		notIndexedParameters.put(IConstants.NAME, indexContext.getName());

		// The start url
		seedUrl(dataBase, indexable);

		final List<Thread> threads = new ArrayList<Thread>();
		String name = this.getClass().getSimpleName();
		for (int i = 0; i < getThreads(); i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					HttpClient httpClient = new HttpClient();
					IContentProvider<IndexableInternet> contentProvider = new InternetContentProvider();
					localUrlIdParameters.set(new HashMap<String, Object>());
					IndexableInternet indexableInternet = (IndexableInternet) SerializationUtilities.clone(indexable);
					indexableInternet.setParent(indexContext);
					while (true) {
						List<Url> urls = getUrlBatch(dataBase, indexableInternet);
						if (urls.isEmpty()) {
							// Check if there are any other threads still working
							// other than this thread of course
							if (isCrawling(threads)) {
								synchronized (this) {
									try {
										wait(1000);
									} catch (Exception e) {
										logger.error("Exception waiting for more resources to crawl : ", e);
									}
								}
							} else {
								break;
							}
						}
						doUrls(dataBase, indexContext, indexable, urls, contentProvider, httpClient);
					}
				}
			}, name + "." + i);
			thread.start();
			threads.add(thread);
		}
		return threads;
	}

	protected void doUrls(final IDataBase dataBase, final IndexContext<?> indexContext, final IndexableInternet indexable,
			final List<Url> urls, final IContentProvider<IndexableInternet> contentProvider, final HttpClient httpClient) {
		if (urls.size() > 0) {
			logger.info("Batch : " + urls.size() + ", first url : " + urls.get(0));
		}
		for (Url url : urls) {
			try {
				if (url == null || url.getUrl() == null) {
					continue;
				}
				handle(dataBase, indexContext, indexable, url, contentProvider, httpClient);
				url.setParsedContent(null);
				url.setRawContent(null);
				url.setTitle(null);
				url.setContentType(null);
			} catch (Exception e) {
				logger.error("Exception doing url : " + url, e);
			}
		}
	}

	protected synchronized List<Url> getUrlBatch(final IDataBase dataBase, final IndexableInternet indexableInternet) {
		try {
			List<Url> urls = dataBase.find(Url.class, Url.SELECT_FROM_URL_BY_NAME_AND_INDEXED, notIndexedParameters, 0,
					indexableInternet.getInternetBatchSize());
			if (urls.isEmpty()) {
				persistBatch(dataBase);
				urls = dataBase.find(Url.class, Url.SELECT_FROM_URL_BY_NAME_AND_INDEXED, notIndexedParameters, 0,
						indexableInternet.getInternetBatchSize());
			}
			for (Url url : urls) {
				url.setIndexed(Boolean.TRUE);
			}
			dataBase.mergeBatch(urls);
			return urls;
		} finally {
			notifyAll();
		}
	}

	protected void handle(final IDataBase dataBase, final IndexContext<?> indexContext, final IndexableInternet indexable, final Url url,
			final IContentProvider<IndexableInternet> contentProvider, final HttpClient httpClient) {
		try {
			// Get the content from the url
			ByteOutputStream byteOutputStream = getContentFromUrl(contentProvider, httpClient, indexable, url);
			if (byteOutputStream == null || byteOutputStream.size() == 0) {
				logger.warn("No content from url, perhaps exception : " + url);
				return;
			}
			InputStream inputStream = new ByteArrayInputStream(byteOutputStream.getBytes());
			// Extract the links from the url if any
			extractLinksFromContent(dataBase, indexable, url, inputStream);

			// Parse the content from the url
			String parsedContent = getParsedContent(url, byteOutputStream);
			if (parsedContent == null) {
				return;
			}
			long hash = HashUtilities.hash(parsedContent);
			url.setHash(hash);
			// Check for duplicates
			if (cache.get(hash) != null) {
				url.setRawContent(null);
				url.setParsedContent(null);
				logger.info("Duplicate data : " + url);
				dataBase.merge(url);
			}
			cache.put(new net.sf.ehcache.Element(hash, url));
			// Add the document to the index
			addDocumentToIndex(indexContext, indexable, url, parsedContent);
		} catch (Exception e) {
			logger.error("Exception visiting page : " + (url != null ? url.getUrl() : null), e);
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
	protected ByteOutputStream getContentFromUrl(final IContentProvider<IndexableInternet> contentProvider, final HttpClient httpClient,
			final IndexableInternet indexable, final Url url) {
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
			logger.error("Exception getting the content from the url : " + url, e);
		} finally {
			try {
				if (get != null) {
					get.releaseConnection();
				}
			} catch (Exception e) {
				logger.error("Exception releasing the connection to the url : " + url, e);
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
			logger.error("Exception accessing url : " + url, e);
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
	protected void addDocumentToIndex(final IndexContext<?> indexContext, final IndexableInternet indexable, final Url url,
			final String parsedContent) {
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
			this.addDocument(indexContext, indexable, document);
		} catch (Exception e) {
			logger.error("Exception accessing url : " + url, e);
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
	protected void extractLinksFromContent(final IDataBase dataBase, final IndexableInternet indexableInternet, final Url baseUrl,
			final InputStream inputStream) {
		try {
			if (localUrlIdParameters.get() == null) {
				localUrlIdParameters.set(new HashMap<String, Object>());
			}
			Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
			Source source = new Source(reader);
			List<Tag> tags = source.getAllTags();
			String baseUrlStripped = indexableInternet.getBaseUrl();
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
							String resolvedLink = UriUtilities.resolve(indexableInternet.getUri(), link);
							String replacement = resolvedLink.contains("?") ? "?" : "";
							String strippedSessionLink = UriUtilities.stripJSessionId(resolvedLink, replacement);
							String strippedAnchorLink = UriUtilities.stripAnchor(strippedSessionLink, "");
							if (!UriUtilities.isInternetProtocol(strippedAnchorLink)) {
								continue;
							}
							if (!strippedAnchorLink.startsWith(baseUrlStripped)) {
								continue;
							}
							if (excludedPattern != null && excludedPattern.matcher(strippedAnchorLink).matches()) {
								continue;
							}
							Long urlId = HashUtilities.hash(strippedAnchorLink);

							// Try the cache first
							if (cache.get(urlId) != null || newCache.get(urlId) != null) {
								continue;
							}
							localUrlIdParameters.get().put(IConstants.URL_ID, urlId);
							Url dbUrl = null;
							try {
								dbUrl = dataBase.find(Url.class, Url.SELECT_FROM_URL_BY_URL_ID, localUrlIdParameters.get());
							} catch (NonUniqueResultException e) {
								continue;
							} catch (NoResultException e) {
								// Swallow
							}
							if (dbUrl != null) {
								cache.put(new net.sf.ehcache.Element(urlId, dbUrl));
								continue;
							}
							Url url = new Url();
							url.setUrlId(urlId);
							url.setName(indexableInternet.getParent().getName());
							url.setIndexed(Boolean.FALSE);
							// Add the link to the database here
							url.setUrl(strippedAnchorLink);
							newCache.put(new net.sf.ehcache.Element(urlId, url));
						} catch (Exception e) {
							logger.error("Exception extracting link : " + tag, e);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("General exception extracting the links from : ", e);
		}
		try {
			if (newCache.getSize() > indexableInternet.getInternetBatchSize()) {
				persistBatch(dataBase);
			}
		} catch (Exception e) {
			logger.error("Exception persisting url batch : ", e);
		}
	}

	protected synchronized void persistBatch(final IDataBase dataBase) {
		try {
			// Persist all the urls in the cache that are not persisted
			List<Url> newUrls = new ArrayList<Url>();
			List<?> keys = newCache.getKeys();
			for (Object key : keys) {
				try {
					net.sf.ehcache.Element element = newCache.get(key);
					// logger.info("Cache element : " + element);
					if (element == null) {
						continue;
					}
					Object objectValue = element.getObjectValue();
					if (objectValue == null || !Url.class.isAssignableFrom(objectValue.getClass())) {
						logger.warn("Cache object value null or not a url : " + objectValue);
						continue;
					}
					Url url = (Url) objectValue;
					newUrls.add(url);
				} catch (Exception e) {
					logger.error("Exception persisting the url batch : ", e);
				}
			}
			newCache.removeAll();
			dataBase.persistBatch(newUrls);
		} finally {
			notifyAll();
		}
	}

	protected boolean isCrawling(final List<Thread> threads) {
		int threadsRunnable = 0;
		for (Thread thread : threads) {
			if (thread.getState().equals(State.RUNNABLE)) {
				threadsRunnable++;
			}
		}
		if (threadsRunnable >= 2) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	protected void seedUrl(final IDataBase dataBase, final IndexableInternet indexableInternet) {
		String urlString = indexableInternet.getUrl();
		indexableInternet.setCurrentUrl(urlString);

		Url url = new Url();
		url.setName(indexableInternet.getParent().getName());
		url.setUrlId(HashUtilities.hash(urlString));
		url.setUrl(urlString);
		url.setIndexed(Boolean.FALSE);

		dataBase.persist(url);
	}

	@Override
	public void addDocument(final IndexContext<?> indexContext, final Indexable<IndexableInternet> indexable, final Document document)
			throws CorruptIndexException, IOException {
		super.addDocument(indexContext, indexable, document);
	}

}