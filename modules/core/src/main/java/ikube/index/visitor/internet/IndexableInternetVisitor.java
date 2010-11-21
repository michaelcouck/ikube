package ikube.index.visitor.internet;

import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.index.visitor.IndexableVisitor;
import ikube.model.IndexableInternet;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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

	private HttpClient httpClient = new DefaultHttpClient();

	@Override
	public void visit(IndexableInternet indexable) {
		try {
			// The start url
			String url = indexable.getUrl();
			HttpGet get = new HttpGet(url);
			HttpResponse httpResponse = httpClient.execute(get);
			InputStream inputStream = httpResponse.getEntity().getContent();
			String contentType = httpResponse.getEntity().getContentType().getValue();
			IParser parser = ParserProvider.getParser(contentType, null);
			OutputStream outputStream = parser.parse(inputStream);
			// TODO - add the content to the index
			// TODO - Add the title field
			// TODO - Add the contents field
			String fieldContents = outputStream.toString();

			Document document = new Document();
			Store store = indexable.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;

			addStringField(indexable.getName(), fieldContents, document, store, analyzed, termVector);

			getIndexContext().getIndexWriter().addDocument(document);
		} catch (Exception e) {
			logger.error("Exception reading the url : " + indexable.getUrl(), e);
		}
	}
}
