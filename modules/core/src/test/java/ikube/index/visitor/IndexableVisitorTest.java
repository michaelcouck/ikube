package ikube.index.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.BaseTest;
import ikube.model.IndexableColumn;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.Reader;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.junit.Before;
import org.junit.Test;

public class IndexableVisitorTest extends BaseTest {

	private IndexableVisitor<?> indexableVisitor;
	private String fieldName = "fieldName";
	private Document document;
	Store store = Store.YES;
	TermVector termVector = TermVector.YES;
	Index index = Index.ANALYZED;

	@Before
	public void before() {
		this.indexableVisitor = new IndexableVisitor<IndexableColumn>() {
			@Override
			public void visit(IndexableColumn indexable) {
			}
		};
		this.document = new Document();
	}

	@Test
	public void addReaderField() throws Exception {
		// We want to add a reader field to the document
		Reader reader = getReader(Reader.class);
		this.indexableVisitor.addReaderField(fieldName, document, store, termVector, reader);

		// Verify that it not null
		Field field = document.getField(fieldName);
		assertNotNull(field);
		document.removeField(fieldName);
		field = document.getField(fieldName);
		assertNull(field);

		// Now we want to add a reader field that will be merged
		Reader fieldReader = getReader(Reader.class);
		field = new Field(fieldName, fieldReader);
		document.add(field);
		this.indexableVisitor.addReaderField(fieldName, document, store, termVector, fieldReader);

		// Verify that it is not null
		Reader finalFieldReader = field.readerValue();
		assertNotNull(finalFieldReader);
	}

	@Test
	public void addStringField() {
		// IndexableColumn, String
		String stringFieldValue = "string field value";
		this.indexableVisitor.addStringField(fieldName, stringFieldValue, document, store, index, termVector);

		// Verify that it not null
		Field field = document.getField(fieldName);
		assertNotNull(field);
		// Verify that the value is the same as the field value string
		assertEquals(stringFieldValue, field.stringValue());

		// Add another field with the same name and
		// verify that the string fields have been merged
		this.indexableVisitor.addStringField(fieldName, stringFieldValue, document, store, index, termVector);

		field = document.getField(fieldName);
		assertNotNull(field);
		// Verify that the value is the same as the field value string
		assertEquals(stringFieldValue + " " + stringFieldValue, field.stringValue());
	}

	@Test
	public void getTempFile() throws Exception {
		// Get the temp file for the field
		File file = this.indexableVisitor.getTempFile(fieldName);
		assertNotNull(file);
		assertTrue(file.exists());
		FileUtilities.deleteFile(file, 3);
		assertFalse(file.exists());
	}

	private <T extends Reader> T getReader(Class<T> t) throws Exception {
		T reader = mock(t);
		when(reader.read(any(char[].class))).thenReturn(-1);
		return reader;
	}

}
