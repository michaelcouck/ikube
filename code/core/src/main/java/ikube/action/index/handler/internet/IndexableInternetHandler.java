package ikube.action.index.handler.internet;

import ikube.IConstants;
import ikube.action.index.content.ByteOutputStream;
import ikube.action.index.content.IContentProvider;
import ikube.action.index.content.InternetContentProvider;
import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.action.index.parse.IParser;
import ikube.action.index.parse.ParserProvider;
import ikube.action.index.parse.XMLParser;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

import javax.swing.text.html.HTML;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriUtils;

public class IndexableInternetHandler extends IndexableHandler<IndexableInternet> {

	private HttpClient httpClient;
	@Autowired
	private InternetResourceHandler internetResourceHandler;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ForkJoinTask<?> handleIndexableForked(final IndexContext<?> indexContext, final IndexableInternet indexableInternet) throws Exception {
		initialize();
		IResourceProvider<Url> internetResourceProvider = new InternetResourceProvider(indexableInternet);
		return getRecursiveAction(indexContext, indexableInternet, internetResourceProvider);
	}

	void initialize() {
		MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams connectionManagerParams = new HttpConnectionManagerParams();
		connectionManagerParams.setDefaultMaxConnectionsPerHost(10000);
		connectionManagerParams.setMaxTotalConnections(100000);
		connectionManagerParams.setStaleCheckingEnabled(true);
		connectionManagerParams.setTcpNoDelay(true);
		multiThreadedHttpConnectionManager.setParams(connectionManagerParams);
		HttpClientParams httpClientParams = new HttpClientParams();
		httpClient = new HttpClient(httpClientParams, multiThreadedHttpConnectionManager);
	}

	@Override
	protected List<Url> handleResource(final IndexContext<?> indexContext, final IndexableInternet indexableInternet, final Object resource) {
		try {
			Url url = (Url) resource;
			IContentProvider<IndexableInternet> contentProvider = new InternetContentProvider();
			return handle(indexContext, indexableInternet, url, contentProvider);
		} catch (Exception e) {
			handleException(indexableInternet, e, "Exception crawling url : " + resource);
		}
		return null;
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
	protected List<Url> handle(final IndexContext<?> indexContext, final IndexableInternet indexable, final Url url,
			final IContentProvider<IndexableInternet> contentProvider) {
		List<Url> extractedUrls = null;
		try {
			// Get the content from the url
			ByteOutputStream byteOutputStream = getContentFromUrl(contentProvider, indexable, url);
			if (byteOutputStream != null && byteOutputStream.size() > 0) {
				indexable.setRawContent(byteOutputStream.getBytes());
				InputStream inputStream = new ByteArrayInputStream(byteOutputStream.getBytes());
				// Extract the links from the url if any
				extractedUrls = extractLinksFromContent(indexable, inputStream);
				// Parse the content from the url
				String parsedContent = getParsedContent(url, byteOutputStream);
				if (parsedContent != null) {
					Long hash = HashUtilities.hash(parsedContent);
					url.setHash(hash);
					// Add the document to the index
					internetResourceHandler.handleResource(indexContext, indexable, new Document(), url);
				}
			}
		} catch (Exception e) {
			handleException(indexable, e);
		} finally {
			url.setRawContent(null);
			url.setParsedContent(null);
		}
		return extractedUrls;
	}

	/**
	 * Gets the raw data from the url.
	 * 
	 * @param indexable the indexable to set the transient data in
	 * @param url the url to get the data from
	 * @return the raw data from the url
	 */
	protected ByteOutputStream getContentFromUrl(final IContentProvider<IndexableInternet> contentProvider, final IndexableInternet indexable, final Url url) {
		GetMethod get = null;
		ByteOutputStream byteOutputStream = null;
		try {
			String encodedUrl = UriUtils.encodeUri(url.getUrl(), IConstants.ENCODING);

			GetMethod getMethod = new GetMethod(encodedUrl);
			httpClient.executeMethod(getMethod);
			InputStream inputStream = getMethod.getResponseBodyAsStream();
			byteOutputStream = new ByteOutputStream();
			FileUtilities.getContents(inputStream, byteOutputStream, Integer.MAX_VALUE);

			url.setRawContent(byteOutputStream.getBytes());
			if (url.getUrl() != null) {
				// Add the url to the content
				byteOutputStream.write(" ".getBytes());
				byteOutputStream.write(url.getUrl().getBytes());
			}
			if (StringUtils.isEmpty(byteOutputStream.toString())) {
				logger.info("No content for url : " + encodedUrl);
			}
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
					String message = "Exception parsing content from url : " + url;
					logger.error(message, e);
					handleException(null, e, message);
				}
			}
			url.setContentType(contentType);
			if (outputStream != null) {
				url.setParsedContent(outputStream.toString());
				return outputStream.toString();
			}
		} catch (NullPointerException e) {
			logger.error(null, e);
			handleException(null, e);
		} catch (Exception e) {
			handleException(null, e);
		}
		return null;
	}

	protected List<Url> extractLinksFromContent(final IndexableInternet indexableInternet, final InputStream inputStream) {
		List<Url> urls = new ArrayList<>();
		try {
			Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
			Source source = new Source(reader);
			List<Tag> tags = source.getAllTags();
			String baseUrlStripped = indexableInternet.getBaseUrl();
			for (final Tag tag : tags) {
				if (tag.getName().equals(HTMLElementName.A) && StartTag.class.isAssignableFrom(tag.getClass())) {
					Attribute attribute = ((StartTag) tag).getAttributes().get(HTML.Attribute.HREF.toString());
					if (attribute == null) {
						continue;
					}
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
						Url url = new Url();
						url.setUrl(strippedAnchorLink);
						urls.add(url);
					} catch (Exception e) {
						handleException(indexableInternet, e);
					}
				}
			}
		} catch (IOException e) {
			handleException(indexableInternet, e);
		}
		return urls;
	}

}