package ikube.action.index.handler.internet;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.content.ByteOutputStream;
import ikube.action.index.content.IContentProvider;
import ikube.action.index.content.InternetContentProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.action.index.handler.ResourceHandlerBase;
import ikube.action.index.parse.IParser;
import ikube.action.index.parse.ParserProvider;
import ikube.action.index.parse.XMLParser;
import ikube.action.index.parse.mime.MimeType;
import ikube.action.index.parse.mime.MimeTypes;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.security.WebServiceAuthentication;
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
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;

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
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is the crawler for internet and intranets sites. There are several levels of caches to improve performance in this class. Firstly the JPA cache provided
 * by the implementation. Then the query cache also from the JPA implementation. There is a new cache that is used to cache new urls to batch them for insert,
 * and then there is the url cache that is added to manually.
 * 
 * This class is optimized for performance, as such the elegance has taken a back seat. To facilitate several hundred million pages performance was by far the
 * most important aspect of this logic. Memory concerns and trips to the database are critical, and we would like to keep both to an absolute minimum, ergo the
 * caches.
 * 
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableInternetHandler extends IndexableHandler<IndexableInternet> {

	@Autowired
	@SuppressWarnings("rawtypes")
	private ResourceHandlerBase resourceUrlHandler;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handleIndexable(final IndexContext<?> indexContext, final IndexableInternet indexable) throws Exception {
		// The start url
		try {
			final ForkJoinPool forkJoinPool = new ForkJoinPool(indexable.getThreads());
			final Stack<Url> in = new Stack<Url>();
			final Set<Long> out = new TreeSet<Long>();
			seedUrl(indexable, in, out);
			RecursiveAction recursiveAction = getRecursiveAction(indexContext, indexable, in, out, forkJoinPool);
			forkJoinPool.invoke(recursiveAction);
			recursiveAction.join();
		} catch (Exception e) {
			handleException(indexable, e);
		}
		return new ArrayList<Future<?>>();
	}

	private RecursiveAction getRecursiveAction(final IndexContext<?> indexContext, final IndexableInternet indexable, final Stack<Url> in, final Set<Long> out,
			final ForkJoinPool forkJoinPool) {
		RecursiveAction recursiveAction = new RecursiveAction() {
			@Override
			protected void compute() {
				logger.info("Started executing : " + in.size() + ", " + this.hashCode());
				IndexableInternet indexableInternet = (IndexableInternet) SerializationUtilities.clone(indexable);
				do {
					try {
						HttpClient httpClient = new HttpClient();
						IContentProvider<IndexableInternet> contentProvider = new InternetContentProvider();
						doUrl(indexContext, indexable, in.pop(), contentProvider, httpClient, in, out);
						if (in.size() > indexableInternet.getInternetBatchSize() * 2 && forkJoinPool.getRunningThreadCount() < indexable.getThreads()) {
							// If there are many urls in the pool then fork off a few threads to handle the excess load,
							// we execute the first, and join the second, with a little luck they will finish at the same time roughly
							RecursiveAction leftRecursiveAction = getRecursiveAction(indexContext, indexableInternet, in, out, forkJoinPool);
							RecursiveAction rightRecursiveAction = getRecursiveAction(indexContext, indexableInternet, in, out, forkJoinPool);
							logger.info("Joining : " + forkJoinPool.getRunningThreadCount() + ", " + rightRecursiveAction.hashCode() + ", " + this.hashCode());
							invokeAll(leftRecursiveAction, rightRecursiveAction);
							logger.info("Finished joining : " + rightRecursiveAction.hashCode() + ", " + this.hashCode());
						}
						logger.info("Done urls : " + out.size());
						logger.info("Still to do urls : " + in.size() + ", initialized : " + ThreadUtilities.isInitialized());
						if (!ThreadUtilities.isInitialized()) {
							throw new InterruptedException("Indexing terminated : ");
						}
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					} catch (CancellationException e) {
						throw new RuntimeException(e);
					} catch (Exception e) {
						handleException(indexable, e, e.getMessage());
					}
				} while (in.size() > 0 && ThreadUtilities.isInitialized());
				logger.info("Finished executing : " + in.size() + ", " + this.hashCode());
			}
		};
		return recursiveAction;
	}

	/**
	 * This method iterates over the batch of urls and indexes the content, also extracting other links from the pages.
	 * 
	 * @param indexContext the index context for this internet url
	 * @param indexable the indexable, which is the url configuration
	 * @param urlBatch the batch of urls to index
	 * @param contentProvider the content provider for http pages
	 * @param httpClient the client to use for accessing the pages over http
	 */
	protected void doUrl(final IndexContext<?> indexContext, final IndexableInternet indexable, Url url,
			final IContentProvider<IndexableInternet> contentProvider, final HttpClient httpClient, Stack<Url> in, Set<Long> out) {
		try {
			Thread.sleep(indexContext.getThrottle());
			handle(indexContext, indexable, url, contentProvider, httpClient, in, out);
		} catch (Exception e) {
			handleException(indexable, e);
		} finally {
			url.setParsedContent(null);
			url.setRawContent(null);
			url.setTitle(null);
			url.setContentType(null);
		}
	}

	/**
	 * This method will do the actions that visit the url, parse the data and add it to the index.
	 * 
	 * @param indexContext the index context for this index
	 * @param indexable the internet base url configuration object
	 * @param url the url that will be indexed in this call
	 * @param contentProvider the content provider for internet http pages
	 * @param httpClient the client for accessing the pages
	 */
	protected void handle(final IndexContext<?> indexContext, final IndexableInternet indexable, final Url url,
			final IContentProvider<IndexableInternet> contentProvider, final HttpClient httpClient, Stack<Url> in, Set<Long> out) {
		try {
			logger.debug("Doing url : " + url.getUrl());
			// Get the content from the url
			ByteOutputStream byteOutputStream = getContentFromUrl(contentProvider, httpClient, indexable, url);
			if (byteOutputStream == null || byteOutputStream.size() == 0) {
				logger.warn("No content from url, perhaps exception : " + url);
				return;
			}
			InputStream inputStream = new ByteArrayInputStream(byteOutputStream.getBytes());
			// Extract the links from the url if any
			extractLinksFromContent(indexable, inputStream, in, out);

			// Parse the content from the url
			String parsedContent = getParsedContent(url, byteOutputStream);
			if (parsedContent == null) {
				return;
			}
			Long hash = HashUtilities.hash(parsedContent);
			url.setHash(hash.longValue());

			// Add the document to the index
			handleResource(indexContext, indexable, new Document(), url);
		} catch (Exception e) {
			handleException(indexable, e);
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
			handleException(indexable, e);
		} finally {
			try {
				if (get != null) {
					get.releaseConnection();
				}
			} catch (Exception e) {
				handleException(indexable, e);
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
					contentType = "text/html";
					parser = ParserProvider.getParser(contentType, bytes);
					outputStream = parser.parse(byteArrayInputStream, new ByteArrayOutputStream());
				} else {
					handleException(null, e);
				}
			}
			url.setContentType(contentType);
			if (outputStream != null) {
				url.setParsedContent(outputStream.toString());
				return outputStream.toString();
			}
		} catch (Exception e) {
			url.setRawContent(null);
			url.setParsedContent(null);
			handleException(null, e);
		}
		return null;
	}

	/**
	 * Adds the document to the index with all the defined fields. Typically the fields are the title, the field names that are defined in the configuration and
	 * the content field name.
	 * 
	 * @param indexable the indexable or base host for this crawl
	 * @param url the url being added to the index, i.e. just been visited and the data has been extracted
	 * @param parsedContent the content that was extracted from the url
	 */
	@SuppressWarnings("unchecked")
	public Document handleResource(final IndexContext<?> indexContext, final IndexableInternet indexable, final Document document, final Object resource) {
		Url url = (Url) resource;
		try {
			Store store = indexable.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;

			// Add the id field, which is the url in this case
			IndexManager.addStringField(indexable.getIdFieldName(), url.getUrl(), document, Store.YES, Index.ANALYZED, TermVector.YES);
			// Add the title field
			MimeType mimeType = MimeTypes.getMimeType(url.getContentType(), url.getRawContent());
			if (mimeType != null && mimeType.getSubType().toLowerCase().contains(HTMLElementName.HTML.toLowerCase())) {
				InputStream inputStream = new ByteArrayInputStream(url.getRawContent());
				Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
				Source source = new Source(reader);
				Element titleElement = source.getNextElement(0, HTMLElementName.TITLE);
				if (titleElement != null) {
					String title = titleElement.getContent().toString();
					url.setTitle(title);
					IndexManager.addStringField(indexable.getTitleFieldName(), title, document, store, analyzed, termVector);
				}
			} else {
				// Add the url as the title
				IndexManager.addStringField(indexable.getTitleFieldName(), url.getUrl(), document, store, analyzed, termVector);
			}
			// Add the contents field
			IndexManager.addStringField(indexable.getContentFieldName(), url.getParsedContent(), document, store, analyzed, termVector);
			resourceUrlHandler.handleResource(indexContext, indexable, document, null);
		} catch (Exception e) {
			handleException(indexable, e);
		}
		return document;
	}

	/**
	 * Extracts all the links from the content and sets them in the cluster wide cache. The cache is persistence backed so any overflow then goes to a local
	 * object oriented database on each server.
	 * 
	 * @param indexableInternet the indexable that is being crawled
	 * @param inputStream the input stream of the data from the base url, i.e. the html
	 */
	protected void extractLinksFromContent(final IndexableInternet indexableInternet, final InputStream inputStream, final Stack<Url> in, final Set<Long> out) {
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

							// Check the out stack for this url
							Long hash = HashUtilities.hash(strippedAnchorLink);
							if (!out.add(hash)) {
								continue;
							}
							Url url = new Url();
							url.setUrlId(urlId.longValue());
							url.setName(indexableInternet.getName());
							url.setIndexed(Boolean.FALSE);
							url.setUrl(strippedAnchorLink);
							// logger.info("Adding url : " + url.getUrl());
							// Add the new url to the cache
							in.push(url);
						} catch (Exception e) {
							handleException(indexableInternet, e);
						}
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			handleException(indexableInternet, e);
		} catch (IOException e) {
			handleException(indexableInternet, e);
		}
	}

	/**
	 * This method will add the first or base url to the database. Typically this method gets called before starting the crawl.
	 * 
	 * @param dataBase the database for the persistence
	 * @param indexableInternet the base url object from the configuration for the site/intranet
	 */
	protected void seedUrl(final IndexableInternet indexableInternet, Stack<Url> in, Set<Long> out) {
		String urlString = indexableInternet.getUrl();
		indexableInternet.setCurrentUrl(urlString);

		Url url = new Url();
		url.setName(indexableInternet.getName());
		url.setUrlId(HashUtilities.hash(urlString).longValue());
		url.setUrl(urlString);
		url.setIndexed(Boolean.FALSE);

		in.push(url);
		out.add(HashUtilities.hash(url.getUrl()));
	}

	public void login(IndexableInternet indexableInternet, HttpClient httpClient) {
		URL loginUrl;
		try {
			loginUrl = new URL(indexableInternet.getLoginUrl());
			String userid = indexableInternet.getUserid();
			String password = indexableInternet.getPassword();
			new WebServiceAuthentication().authenticate(httpClient, loginUrl.getHost(), Integer.toString(loginUrl.getPort()), userid, password);
		} catch (Exception e) {
			handleException(indexableInternet, e);
		}
	}

	@Override
	protected List<?> handleResource(IndexContext<?> indexContext, IndexableInternet indexableInternet, Object resource) {
		return null;
	}

}