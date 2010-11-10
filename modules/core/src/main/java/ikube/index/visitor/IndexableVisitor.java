package ikube.index.visitor;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

public abstract class IndexableVisitor<I extends Indexable<?>> {

	protected Logger logger = Logger.getLogger(this.getClass());
	private String indexableType;
	protected IndexContext indexContext;
	private Map<String, File> tempFiles;
	{
		this.tempFiles = new HashMap<String, File>();
	}

	public String getIndexableType() {
		return indexableType;
	}

	public void setIndexableType(String indexableType) {
		this.indexableType = indexableType;
	}

	public IndexContext getIndexContext() {
		return indexContext;
	}

	public void setIndexContext(IndexContext indexContext) {
		this.indexContext = indexContext;
	}

	public abstract void visit(I indexable);

	protected void addStringField(String fieldName, String fieldContent, Document document, Store store, Index analyzed,
			TermVector termVector) {
		Field field = document.getField(fieldName);
		if (field == null) {
			field = new Field(fieldName, fieldContent, store, analyzed, termVector);
			document.add(field);
		} else {
			String fieldValue = field.stringValue();
			StringBuilder builder = new StringBuilder(fieldValue).append(" ").append(fieldContent);
			field.setValue(builder.toString());
		}
	}

	protected void addReaderField(String fieldName, Document document, Store store, TermVector termVector, Reader reader) throws Exception {
		Field field = document.getField(fieldName);
		if (field == null) {
			field = new Field(fieldName, reader, termVector);
			document.add(field);
		} else {
			Reader fieldReader = field.readerValue();
			if (fieldReader == null) {
				fieldReader = new StringReader(field.stringValue());
			}
			File tempFile = getTempFile(fieldName);
			Writer writer = new FileWriter(tempFile, false);
			int read = -1;
			char[] chars = new char[1024];
			while ((read = fieldReader.read(chars)) > -1) {
				writer.write(chars, 0, read);
			}
			while ((read = reader.read(chars)) > -1) {
				writer.write(chars, 0, read);
			}
			Reader finalReader = new FileReader(tempFile);
			// This is a string field, and could be stored so we check that
			if (store.isStored()) {
				// Remove the field and add it again
				document.removeField(fieldName);
				field = new Field(fieldName, finalReader, termVector);
				document.add(field);
			} else {
				field.setValue(finalReader);
			}
		}
	}

	protected File getTempFile(String fieldName) throws Exception {
		File tempFile = tempFiles.get(fieldName);
		if (tempFile == null) {
			tempFile = File.createTempFile(Long.toString(System.nanoTime()), IConstants.READER_FILE_SUFFIX);
			tempFiles.put(fieldName, tempFile);
		}
		return tempFile;
	}

}
