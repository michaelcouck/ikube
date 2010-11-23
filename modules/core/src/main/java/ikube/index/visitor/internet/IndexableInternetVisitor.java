package ikube.index.visitor.internet;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.index.visitor.IndexableVisitor;
import ikube.listener.ListenerManager;
import ikube.model.Event;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableInternetVisitor<T> extends IndexableVisitor<IndexableInternet> {

	private HttpClient httpClient;
	private IDataBase dataBase;

	public IndexableInternetVisitor() {
		this.httpClient = new DefaultHttpClient();
	}

	@Override
	public void visit(IndexableInternet indexable) {
		try {
			// The start url
			String urlString = indexable.getUrl();
			Url url = new Url();
			url.setUrl(urlString);
			url.setName(getIndexContext().getIndexName());
			indexable.setCurrentUrl(urlString);
			dataBase.persist(url);
			do {
				visitUrl(indexable, url);
				url.setIndexed(Boolean.TRUE);
				dataBase.merge(url);
				// Get the next url if there is one
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put(IConstants.NAME, indexable.getName());
				parameters.put(IConstants.INDEXED, Boolean.FALSE);
				url = dataBase.find(Url.class, parameters, Boolean.FALSE);
			} while (url != null);
		} catch (Exception e) {
			logger.error("Exception reading the url : " + indexable.getUrl(), e);
		}
	}

	protected void visitUrl(IndexableInternet indexable, Url url) throws Exception {
		logger.debug("Doing url : " + url.getUrl());
		HttpGet get = new HttpGet(url.getUrl());
		HttpResponse httpResponse = httpClient.execute(get);
		InputStream inputStream = httpResponse.getEntity().getContent();
		String contentType = httpResponse.getEntity().getContentType().getValue();

		if (contentType == null) {
			contentType = get.getURI().toURL().getFile();
		}

		ByteArrayOutputStream byteArrayOutputStream = FileUtilities.getContents(inputStream, Integer.MAX_VALUE);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

		byte[] bytes = new byte[1024];
		byteArrayInputStream.mark(bytes.length);
		byteArrayInputStream.read(bytes);
		byteArrayInputStream.reset();

		IParser parser = ParserProvider.getParser(contentType, bytes);
		OutputStream outputStream = parser.parse(byteArrayInputStream);
		// TODO - Add the title field
		// TODO - Add the contents field
		String fieldContents = outputStream.toString();
		logger.debug("Parsed : " + fieldContents);

		Document document = new Document();
		Store store = indexable.isStored() ? Store.YES : Store.NO;
		Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
		TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;

		addStringField(indexable.getName(), fieldContents, document, store, analyzed, termVector);

		getIndexContext().getIndexWriter().addDocument(document);

		byteArrayInputStream.reset();
		extractLinks(indexable, byteArrayInputStream);
	}

	protected void extractLinks(IndexableInternet indexable, InputStream inputStream) throws Exception {
		// Extract the links
		Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
		Source source = new Source(reader);
		List<Tag> tags = source.getAllTags();
		URI baseURI = new URI(indexable.getCurrentUrl());
		for (Tag tag : tags) {
			if (tag.getName().equals(HTMLElementName.A)) {
				logger.debug("Tag : " + tag);
				if (StartTag.class.isAssignableFrom(tag.getClass())) {
					Attribute attribute = ((StartTag) tag).getAttributes().get("href");
					if (attribute != null) {
						String reference = attribute.getValue();
						Url newUrl = new Url();
						if (!reference.startsWith("http")) {
							URI uri = URIUtils.resolve(baseURI, reference);
							newUrl.setUrl(uri.toString());
						} else {
							String baseHost = baseURI.getHost();
							if (!reference.contains(baseHost)) {
								continue;
							}
						}
						newUrl.setName(indexable.getName());
						logger.debug("Found url : " + newUrl.getUrl());
						Event event = new Event();
						event.setObject(newUrl);
						event.setTimestamp(new Timestamp(System.currentTimeMillis()));
						event.setType(Event.LINK);
						ListenerManager.fireEvent(event);
					}
				}
			}
		}
	}

	public void setDataBase(IDataBase dataBase) {
		this.dataBase = dataBase;
	}

}