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
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
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
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
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

	private Cache cache;
	private Cache newCache;

	public IndexableInternetHandler() {
		CacheManager cacheManager = CacheManager.create();
		cache = cacheManager.getCache("UrlCache");
		newCache = cacheManager.getCache("NewUrlCache");
	}

	class IndexableInternetHandlerWorker implements Runnable {

		boolean waiting;
		IndexableInternet indexableInternet;
		IndexContext<?> indexContext;
		List<IndexableInternetHandlerWorker> handlerWorkers;

		IndexableInternetHandlerWorker(IndexContext<?> indexContext, IndexableInternet indexable,
				List<IndexableInternetHandlerWorker> handlerWorkers) {
			this.indexContext = indexContext;
			this.indexableInternet = (IndexableInternet) SerializationUtilities.clone(indexable);
			this.indexableInternet.setParent(indexContext);
			this.handlerWorkers = handlerWorkers;
			this.waiting = Boolean.FALSE;
		}

		public void run() {
			HttpClient httpClient = new HttpClient();
			IContentProvider<IndexableInternet> contentProvider = new InternetContentProvider();
			if (indexableInternet.getLoginUrl() != null) {
				login(indexableInternet, httpClient);
			}
			while (true) {
				List<Url> urls = getUrlBatch(dataBase, indexableInternet);
				if (urls.isEmpty()) {
					// Check if there are any other threads still working
					// other than this thread of course
					waiting = Boolean.TRUE;
					if (areRunning(handlerWorkers)) {
						synchronized (this) {
							try {
								wait(1000);
							} catch (Exception e) {
								logger.error("Exception waiting for more resources to crawl : ", e);
							}
						}
					} else {
						// Return and die
						cache.removeAll();
						break;
					}
				}
				try {
					doUrls(dataBase, indexContext, indexableInternet, urls, contentProvider, httpClient);
				} catch (InterruptedException e) {
					logger.error("Indeing terminated : ", e);
					return;
				}
			}
		}

		/**
		 * This method checks to see that there is at least one other thread that is still in the runnable state. If there are no other
		 * threads in the runnable state then all the urls have been visited on this base url. There will also be no more urls added so we
		 * can exit this thread.
		 * 
		 * @param handlerWorkers the threads to check for the runnable state
		 * @return true if there is at least one other thread that is in the runnable state
		 */
		protected boolean areRunning(final List<IndexableInternetHandlerWorker> handlerWorkers) {
			for (IndexableInternetHandlerWorker handlerWorker : handlerWorkers) {
				if (!handlerWorker.waiting) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handle(final IndexContext<?> indexContext, final IndexableInternet indexable) throws Exception {
		cache.removeAll();
		newCache.removeAll();
		// The start url
		seedUrl(dataBase, indexable);
		List<Future<?>> futures = new ArrayList<Future<?>>();
		try {
			List<IndexableInternetHandlerWorker> handlerWorkers = new ArrayList<IndexableInternetHandler.IndexableInternetHandlerWorker>();
			for (int i = 0; i < getThreads(); i++) {
				IndexableInternetHandlerWorker handlerWorker = new IndexableInternetHandlerWorker(indexContext, indexable, handlerWorkers);
				handlerWorkers.add(handlerWorker);
				Future<?> future = ThreadUtilities.submit(handlerWorker);
				futures.add(future);
			}
		} catch (Exception e) {
			logger.error("Exception starting the internet handler threads : ", e);
		}
		return futures;
	}

	/**
	 * This method iterates over the batch of urls and indexes the content, also extracting other links from the pages.
	 * 
	 * @param dataBase the database to use for persistence
	 * @param indexContext the index context for this internet url
	 * @param indexable the indexable, which is the url configuration
	 * @param urlBatch the batch of urls to index
	 * @param contentProvider the content provider for http pages
	 * @param httpClient the client to use for accessing the pages over http
	 * @throws InterruptedException
	 */
	protected void doUrls(final IDataBase dataBase, final IndexContext<?> indexContext, final IndexableInternet indexable,
			final List<Url> urlBatch, final IContentProvider<IndexableInternet> contentProvider, final HttpClient httpClient)
			throws InterruptedException {
		for (Url url : urlBatch) {
			try {
				if (url == null || url.getUrl() == null) {
					logger.warn("Null url : " + url);
					continue;
				}
				handle(dataBase, indexContext, indexable, url, contentProvider, httpClient);
			} catch (InterruptedException e) {
				throw e;
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
	 * @param dataBase the database to persistence
	 * @param indexableInternet the base indexable for the url
	 * @return the list of urls that have not been visited, this list could be empty if there are no urls that have not been visited
	 */
	protected synchronized List<Url> getUrlBatch(final IDataBase dataBase, final IndexableInternet indexableInternet) {
		try {
			// Get the next batch
			String[] names = new String[] { IConstants.NAME, IConstants.INDEXED };
			Object[] values = new Object[] { indexableInternet.getName(), Boolean.FALSE };
			List<Url> urls = dataBase.find(Url.class, Url.SELECT_FROM_URL_BY_NAME_AND_INDEXED, names, values, 0,
					indexableInternet.getInternetBatchSize());
			if (urls.isEmpty()) {
				// If there are no urls that need to be indexed then empty the new url cache
				// into the database and try again
				persistBatch(dataBase);
				urls = dataBase.find(Url.class, Url.SELECT_FROM_URL_BY_NAME_AND_INDEXED, names, values, 0,
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
	 * @param dataBase the database for the persistence
	 * @param indexContext the index context for this index
	 * @param indexable the internet base url configuration object
	 * @param url the url that will be indexed in this call
	 * @param contentProvider the content provider for internet http pages
	 * @param httpClient the client for accessing the pages
	 * @throws InterruptedException
	 */
	protected void handle(final IDataBase dataBase, final IndexContext<?> indexContext, final IndexableInternet indexable, final Url url,
			final IContentProvider<IndexableInternet> contentProvider, final HttpClient httpClient) throws InterruptedException {
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
					url.setRawContent(null);
					url.setParsedContent(null);
					logger.info("Duplicate url or data : " + url, e);
					return;
				} catch (OptimisticLockException e) {
					// TODO We should re-try this url a certain number of times
					logger.warn("Optimistic lock : " + url, e);
					return;
				}
				cache.put(new net.sf.ehcache.Element(hash, url));
			}
			// Add the document to the index
			addDocument(indexContext, indexable, url, parsedContent);
			Thread.sleep(indexContext.getThrottle());
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Exception visiting page : " + (url != null ? url.getUrl() : null), e);
		}
	}

	/**
	 * Gets the raw data from the url.
	 * 
	 * @param indexable the indexable to set the transient data in
	 * @param url the url to get the data from
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
	 * @param url the url where the data is
	 * @param byteOutputStream the output stream of data from the url
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
	 * @param indexable the indexable or base host for this crawl
	 * @param url the url being added to the index, i.e. just been visited and the data has been extracted
	 * @param parsedContent the content that was extracted from the url
	 */
	protected void addDocument(final IndexContext<?> indexContext, final IndexableInternet indexable, final Url url,
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
			if (mimeType != null && mimeType.getPrimaryType().toLowerCase().contains(HTMLElementName.HTML.toLowerCase())) {
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
			this.addDocument(indexContext, document);
		} catch (Exception e) {
			if (url != null) {
				url.setParsedContent(null);
				url.setRawContent(null);
			}
			logger.error("Exception accessing url : " + url, e);
		}
	}

	/**
	 * Extracts all the links from the content and sets them in the cluster wide cache. The cache is persistence backed so any overflow then
	 * goes to a local object oriented database on each server.
	 * 
	 * @param indexableInternet the indexable that is being crawled
	 * @param baseUrl the base url that the link was found in
	 * @param inputStream the input stream of the data from the base url, i.e. the html
	 */
	protected void extractLinksFromContent(final IDataBase dataBase, final IndexableInternet indexableInternet, final Url baseUrl,
			final InputStream inputStream) {
		try {
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
							Url dbUrl = null;
							try {
								String[] names = new String[] { IConstants.URL_ID };
								Object[] values = new Object[] { urlId };
								dbUrl = dataBase.find(Url.class, Url.SELECT_FROM_URL_BY_URL_ID, names, values);
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
		} catch (UnsupportedEncodingException e) {
			logger.error("Un-supported encoding : ", e);
		} catch (IOException e) {
			logger.error("IOException getting links : ", e);
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
	 * @param dataBase the database to persist the batch of new urls to
	 */
	protected synchronized void persistBatch(final IDataBase dataBase) {
		try {
			// Persist all the urls in the cache that are not persisted
			List<Url> newUrls = new ArrayList<Url>();
			List<?> keys = newCache.getKeys();
			for (Object key : keys) {
				try {
					net.sf.ehcache.Element element = newCache.get(key);
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
			dataBase.persistBatch(newUrls);
			for (Object key : keys) {
				newCache.remove(key);
			}
		} finally {
			notifyAll();
		}
	}

	/**
	 * This method will add the first or base url to the database. Typically this method gets called before starting the crawl.
	 * 
	 * @param dataBase the database for the persistence
	 * @param indexableInternet the base url object from the configuration for the site/intranet
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

	protected void login(IndexableInternet indexableInternet, HttpClient httpClient) {
		List<String> authPrefs = new ArrayList<String>(2);
		authPrefs.add(AuthPolicy.DIGEST);
		authPrefs.add(AuthPolicy.BASIC);
		httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
		httpClient.getParams().setAuthenticationPreemptive(true);
		URL loginUrl;
		try {
			loginUrl = new URL(indexableInternet.getLoginUrl());
			AuthScope authScope = new AuthScope(loginUrl.getHost(), loginUrl.getPort(), AuthScope.ANY_REALM);
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(indexableInternet.getUserid(),
					indexableInternet.getPassword());
			httpClient.getState().setCredentials(authScope, credentials);
		} catch (MalformedURLException e) {
			logger.error("Exception logging in to site : " + indexableInternet, e);
		}
	}

	@Override
	public void addDocument(IndexContext<?> indexContext, Document document) throws CorruptIndexException, IOException {
		indexContext.getIndex().getIndexWriter().addDocument(document);
	}

}