package ikube.index.visitor.database;

import ikube.IConstants;
import ikube.index.content.ColumnContentProvider;
import ikube.index.content.IContentProvider;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.index.visitor.IndexableVisitor;
import ikube.model.IndexableColumn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;

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
			Object content = contentProvider.getContent(indexable);
			if (content == null) {
				return;
			}
			String fieldName = indexable.getFieldName() != null ? indexable.getFieldName() : indexable.getName();
			Store store = indexable.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;

			String mimeType = null;
			if (indexable.getIndexableColumn() != null) {
				if (indexable.getIndexableColumn().getObject() != null) {
					mimeType = indexable.getIndexableColumn().getObject().toString();
					// logger.debug("Got mime type : " + mimeType);
				}
			}

			// logger.debug("Content : " + content);

			byte[] bytes = new byte[1024];
			InputStream inputStream = null;

			if (String.class.isAssignableFrom(content.getClass())) {
				bytes = ((String) content).getBytes(IConstants.ENCODING);
				inputStream = new ByteArrayInputStream(bytes);
			} else if (InputStream.class.isAssignableFrom(content.getClass())) {
				inputStream = (InputStream) content;
				if (FileInputStream.class.isAssignableFrom(inputStream.getClass())) {
					ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
					((FileInputStream) inputStream).getChannel().read(byteBuffer);
					((FileInputStream) inputStream).getChannel().position(0);
					bytes = byteBuffer.array();
				} else if (ByteArrayInputStream.class.isAssignableFrom(inputStream.getClass())) {
					((ByteArrayInputStream) inputStream).mark(bytes.length);
					((ByteArrayInputStream) inputStream).read(bytes);
					((ByteArrayInputStream) inputStream).reset();
				}
			}

			// logger.debug("Bytes : " + new String(bytes, IConstants.ENCODING));
			IParser parser = ParserProvider.getParser(mimeType, bytes);
			OutputStream parsedOutputStream = parser.parse(inputStream);
			// logger.debug("After parse : " + parsedOutputStream);

			if (ByteArrayOutputStream.class.isAssignableFrom(parsedOutputStream.getClass())) {
				String fieldContent = parsedOutputStream.toString();
				addStringField(fieldName, fieldContent, document, store, analyzed, termVector);
			} else if (FileOutputStream.class.isAssignableFrom(parsedOutputStream.getClass())) {
				Reader reader = new InputStreamReader(inputStream);
				addReaderField(fieldName, document, store, termVector, reader);
			} else {
				logger.error("Type not supported from the parser : " + content.getClass().getName());
			}
		} catch (Exception e) {
			logger.error("Exception accessing the column content : ", e);
		}
	}

	protected void setDocument(Document document) {
		this.document = document;
	}

}