package ikube.index.visitor.database;

import ikube.index.content.ColumnContentProvider;
import ikube.index.content.IContentProvider;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.index.visitor.IndexableVisitor;
import ikube.model.IndexableColumn;

import java.io.Reader;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

public class IndexableColumnVisitor<I> extends IndexableVisitor<IndexableColumn> {

	private Logger logger;
	private IContentProvider<IndexableColumn> contentProvider;
	private Document document;

	public IndexableColumnVisitor() {
		this.logger = Logger.getLogger(this.getClass());
		this.contentProvider = new ColumnContentProvider();
	}

	@Override
	public void visit(IndexableColumn indexable) {
		try {
			Object result = contentProvider.getContent(indexable);
			if (result == null) {
				return;
			}
			String fieldName = indexable.getFieldName() != null ? indexable.getFieldName() : indexable.getName();
			Store store = indexable.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;
			if (String.class.isAssignableFrom(result.getClass())) {
				// Parse the content
				String string = (String) result;
				byte[] bytes = string.getBytes();
				IParser parser = ParserProvider.getParser(null, bytes);
				String fieldContent = parser.parse(string);
				addStringField(fieldName, fieldContent, document, store, analyzed, termVector);
			} else if (Reader.class.isAssignableFrom(result.getClass())) {
				addReaderField(fieldName, document, store, termVector, (Reader) result);
			} else {
				logger.warn("Unsupported return type from content provider : ");
			}
		} catch (Exception e) {
			logger.error("Exception accessing the column content : ", e);
		}
	}

	protected void setDocument(Document document) {
		this.document = document;
	}

}
