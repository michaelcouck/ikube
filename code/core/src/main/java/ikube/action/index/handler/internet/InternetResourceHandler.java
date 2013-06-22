package ikube.action.index.handler.internet;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.ResourceHandler;
import ikube.action.index.parse.mime.MimeType;
import ikube.action.index.parse.mime.MimeTypes;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * @author Michael Couck
 * @since 21.06.13
 * @version 01.00
 */
public class InternetResourceHandler extends ResourceHandler<IndexableInternet> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Document handleResource(final IndexContext<?> indexContext, final IndexableInternet indexable, final Document document, final Object resource)
			throws Exception {

		Url url = (Url) resource;

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

		super.addDocument(indexContext, indexable, document);

		return document;
	}

}
