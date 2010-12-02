package ikube.index.handler.internet;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.index.content.ByteOutputStream;
import ikube.index.content.IContentProvider;
import ikube.index.content.InternetContentProvider;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.model.Cache;
import ikube.model.IndexContext;
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
import java.net.URI;
import java.util.List;

import javax.swing.text.html.HTML;

import net.htmlparser.jericho.Attribute;
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

public class UrlHandler implements Runnable {

	private Logger logger;
	private List<Thread> threads;
	private IndexContext indexContext;
	private IndexableInternet indexableInternet;
	private IContentProvider<IndexableInternet> contentProvider;

	public UrlHandler(IndexContext indexContext, IndexableInternet indexableInternet, IDataBase dataBase, List<Thread> threads) {
		this.logger = Logger.getLogger(this.getClass());
		this.threads = threads;
		this.indexContext = indexContext;
		this.indexableInternet = indexableInternet;
		this.contentProvider = new InternetContentProvider();
	}

	public void run() {
		HttpClient httpClient = new HttpClient();
		Cache cache = indexContext.getCache();
		while (true) {
			List<Url> urls = cache.getUrlBatch(threads);
			if (urls.size() == 0) {
				// If we have no more urls it means that
				// there are not more in the cache and all the
				// other threads are waiting too, so we die
				return;
			}
			for (Url url : urls) {
				handleUrl(indexContext, indexableInternet, url, httpClient);
			}
		}
	}

	protected void handleUrl(IndexContext indexContext, IndexableInternet indexable, Url url, HttpClient httpClient) {
		logger.debug("Doing url : " + url.getUrl() + ", " + Thread.currentThread());
		GetMethod get = null;
		ByteOutputStream byteOutputStream = null;
		try {
			get = new GetMethod(url.getUrl());
			httpClient.executeMethod(get);
			InputStream responseInputStream = get.getResponseBodyAsStream();

			indexable.setCurrentInputStream(responseInputStream);

			String contentType = URI.create(url.getUrl()).toURL().getFile();

			byteOutputStream = new ByteOutputStream();
			contentProvider.getContent(indexable, byteOutputStream);

			// The actual byte buffer of data
			byte[] buffer = byteOutputStream.getBytes();
			// The first few bytes so we can guess the content type
			byte[] bytes = new byte[Math.min(buffer.length, 1024)];
			System.arraycopy(buffer, 0, bytes, 0, bytes.length);

			IParser parser = ParserProvider.getParser(contentType, bytes);

			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer, 0, byteOutputStream.getCount());
			OutputStream outputStream = parser.parse(byteArrayInputStream, new ByteArrayOutputStream());
			// TODO - Add the title field
			// TODO - Add the contents field
			String fieldContents = outputStream.toString();

			Long hash = HashUtilities.hash(fieldContents);
			url.setHash(hash);

			Cache cache = indexContext.getCache();
			Url duplicate = cache.getUrlWithHash(url);
			if (duplicate != null) {
				logger.debug("Found duplicate data : " + duplicate + ", url : " + url);
				return;
			}

			Document document = new Document();
			Store store = indexable.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;

			setIdField(indexable, document);
			IndexManager.addStringField(indexable.getName(), fieldContents, document, store, analyzed, termVector);

			indexContext.getIndexWriter().addDocument(document);

			byteArrayInputStream.reset();
			extractLinks(indexable, url, byteArrayInputStream);
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
							if (UriUtilities.isExcluded(link.trim().toLowerCase())) {
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

							Cache cache = indexContext.getCache();
							cache.setUrl(newUrl);
						} catch (Exception e) {
							logger.error("Exception extracting link : " + tag, e);
						}
					}
				}
			}
		}
	}

}