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
 * This is the crawler for internet and intranets sites. There are several levels of caches to improve performance in this class. Firstly
 * the JPA cache provided by the implementation. Then the query cache also from the JPA implementation. There is a new cache that is used to
 * cache new urls to batch them for insert, and then there is the url cache that is added to manually.
 * 
 * This class is optimised for performance, as such the elegance has taken a back seat. To facilitate several hundred million pages
 * performance was by far the most important aspect of this logic. Memory concerns and trips to the database are critical, and we would like
 * to keep both to an absolute minimum, ergo the caches.
 * 
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableInternetHandler extends IndexableHandler<IndexableInternet> {

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
		notIndexedParameters = new HashMap<String, Object>();
		notIndexedParameters.put(IConstants.INDEXED, Boolean.FALSE);
		notIndexedParameters.put(IConstants.NAME, indexable.getName());
		final IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);

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
								cache.removeAll();
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

	/**
	 * This method iterates over the batch of urls and indexes the content, also extracting other links from the pages.
	 * 
	 * @param dataBase
	 *            the database to use for persistence
	 * @param indexContext
	 *            the index context for this internet url
	 * @param indexable
	 *            the indexable, which is the url configuration
	 * @param urlBatch
	 *            the batch of urls to index
	 * @param contentProvider
	 *            the content provider for http pages
	 * @param httpClient
	 *            the client to use for accessing the pages over http
	 */
	protected void doUrls(final IDataBase dataBase, final IndexContext<?> indexContext, final IndexableInternet indexable,
			final List<Url> urlBatch, final IContentProvider<IndexableInternet> contentProvider, final HttpClient httpClient) {
		if (urlBatch.size() > 0) {
			logger.info("Batch : " + urlBatch.size() + ", first url : " + urlBatch.get(0));
		}
		for (Url url : urlBatch) {
			try {
				if (url == null || url.getUrl() == null) {
					continue;
				}
				handle(dataBase, indexContext, indexable, url, contentProvider, httpClient);
			} catch (Exception e) {
				logger.error("Exception doing url : " + url, e);
			} finally {
				try {
					if (url != null) {
						url.setParsedContent(null);
						url.setRawContent(null);
						url.setTitle(null);
						url.setContentType(null);
					}
				} catch (Exception e) {
					logger.error("Exception nulling the fields in the url : " + url, e);
				}
			}
		}
		try {
			dataBase.mergeBatch(urlBatch);
		} catch (Exception e) {
			logger.error("Exception merging urls : ", e);
		}
	}

	/**
	 * This method gets the next batch of urls from the database that have not been visited yet in this iteration. The urls that are
	 * returned will have had the indexed flag set to true and merged back into the database.
	 * 
	 * @param dataBase
	 *            the database to persistence
	 * @param indexableInternet
	 *            the base indexable for the url
	 * @return the list of urls that have not been visited, this list could be empty if there are no urls that have not been visited
	 */
	protected synchronized List<Url> getUrlBatch(final IDataBase dataBase, final IndexableInternet indexableInternet) {
		try {
			// Get the next batch
			List<Url> urls = dataBase.find(Url.class, Url.SELECT_FROM_URL_BY_NAME_AND_INDEXED, notIndexedParameters, 0,
					indexableInternet.getInternetBatchSize());
			if (urls.isEmpty()) {
				// If there are no urls that need to be indexed than empty the new url cache
				// into the database and try again
				persistBatch(dataBase);
				urls = dataBase.find(Url.class, Url.SELECT_FROM_URL_BY_NAME_AND_INDEXED, notIndexedParameters, 0,
						indexableInternet.getInternetBatchSize());
			}
			// Set all the indexed flags to true
			for (Url url : urls) {
				url.setIndexed(Boolean.TRUE);
			}
			dataBase.mergeBatch(urls);
			return urls;
		} finally {
			notifyAll();
		}
	}

	/**
	 * This method will do the actions that visit the url, parse the data and add it to the index.
	 * 
	 * @param dataBase
	 *            the database for the persistence
	 * @param indexContext
	 *            the index context for this index
	 * @param indexable
	 *            the internet base url configuration object
	 * @param url
	 *            the url that will be indexed in this call
	 * @param contentProvider
	 *            the content provider for internet http pages
	 * @param httpClient
	 *            the client for accessing the pages
	 */
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
			net.sf.ehcache.Element cacheElement = cache.get(hash);
			if (cacheElement != null) {
				return;
			} else {
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put(IConstants.HASH, hash);
				try {
					Url dbUrl = dataBase.find(Url.class, Url.SELECT_FROM_URL_BY_HASH, parameters);
					if (dbUrl != null) {
						cache.put(new net.sf.ehcache.Element(hash, dbUrl));
						return;
					}
				} catch (NoResultException e) {
					// Nothing in the db with the hash
				} catch (NonUniqueResultException e) {
					// More than one? Shouldn't be
					logger.warn("Duplicate url or data : " + url, e);
					return;
				}
				cache.put(new net.sf.ehcache.Element(hash, url));
			}
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
			if (url.getUrl() != null) {
				// Add the url to the content
				byteOutputStream.write(" ".getBytes());
				byteOutputStream.write(url.getUrl().getBytes());
			}
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
	 * Adds the document to the index with all the defined fields. Typically the fields are the title, the field names that are defined in
	 * the configuration and the content field name.
	 * 
	 * @param indexable
	 *            the indexable or base host for this crawl
	 * @param url
	 *            the url being added to the index, i.e. just been visited and the data has been extracted
	 * @param parsedContent
	 *            the content that was extracted from the url
	 */
	protected void addDocumentToIndex(final IndexContext<?> indexContext, final IndexableInternet indexable, final Url url,
			final String parsedContent) {
		try {
			Document document = new Document();
			Store store = indexable.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;

			// Add the id field, which is the url in this case
			IndexManager.addStringField(indexable.getIdFieldName(), url.getUrl(), document, Store.YES, Index.ANALYZED, TermVector.YES);
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
			// Add the contents field
			IndexManager.addStringField(indexable.getContentFieldName(), parsedContent, document, store, analyzed, termVector);
			this.addDocument(indexContext, indexable, document);
		} catch (Exception e) {
			logger.error("Exception accessing url : " + url, e);
		}
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
							if (indexableInternet.isExcluded(strippedAnchorLink)) {
								continue;
							}
							Long urlId = HashUtilities.hash(strippedAnchorLink);

							// Try the cache first
							if (cache.get(urlId) != null || newCache.get(urlId) != null) {
								continue;
							}
							// Check the database for this url
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
							url.setName(indexableInternet.getName());
							url.setIndexed(Boolean.FALSE);
							url.setUrl(strippedAnchorLink);
							// Add the new url to the cache, we'll batch them in an insert later
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

	/**
	 * This method will take all the new urls that were added to the new url cache and persist them in a batch, then clear the cache for the
	 * next batch.
	 * 
	 * @param dataBase
	 *            the database to persist the batch of new urls to
	 */
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

	/**
	 * This method checks to see that there is at least one other thread that is still in the runnable state. If there are no other threads
	 * in the runnable state then all the urls have been visited on this base url. There will also be no more urls added so we can exit this
	 * thread.
	 * 
	 * @param threads
	 *            the threads to check for the runnable state
	 * @return true if there is at least one other thread that is in the runnable state
	 */
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

	/**
	 * This method will add the first or base url to the database. Typically this method gets called before starting the crawl.
	 * 
	 * @param dataBase
	 *            the database for the persistence
	 * @param indexableInternet
	 *            the base url object from the configuration for the site/intranet
	 */
	protected void seedUrl(final IDataBase dataBase, final IndexableInternet indexableInternet) {
		String urlString = indexableInternet.getUrl();
		indexableInternet.setCurrentUrl(urlString);

		Url url = new Url();
		url.setName(indexableInternet.getName());
		url.setUrlId(HashUtilities.hash(urlString));
		url.setUrl(urlString);
		url.setIndexed(Boolean.FALSE);

		dataBase.persist(url);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addDocument(final IndexContext<?> indexContext, final Indexable<IndexableInternet> indexable, final Document document)
			throws CorruptIndexException, IOException {
		super.addDocument(indexContext, indexable, document);
	}

}