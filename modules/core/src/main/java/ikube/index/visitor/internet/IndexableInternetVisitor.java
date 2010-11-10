package ikube.index.visitor.internet;

import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.index.visitor.IndexableVisitor;
import ikube.model.IndexableInternet;
import ikube.toolkit.FileUtilities;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

public class IndexableInternetVisitor extends IndexableVisitor<IndexableInternet> {

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
			String content = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toString();
			IParser parser = ParserProvider.getParser(contentType, content.getBytes());
			String parsedContent = parser.parse(content);
			// TODO - add the content to the index
			Document document = new Document();
			Store store = indexable.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;
			// Add the title field
			// Add the contents field
			addStringField(indexable.getName(), parsedContent, document, store, analyzed, termVector);
		} catch (Exception e) {
			logger.error("Exception reading the url : " + indexable.getUrl(), e);
		}
	}
}
