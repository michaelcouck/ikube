package ikube.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.Reader;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class IndexManagerTest extends ATest {

	private String fieldName = "fieldName";
	private Document document = new Document();
	private Store store = Store.YES;
	private TermVector termVector = TermVector.YES;
	private Index index = Index.ANALYZED;

	public IndexManagerTest() {
		super(IndexManagerTest.class);
	}

	@Test
	public void openIndexWriter() throws Exception {
		// String, IndexContext, long
		IndexWriter indexWriter = IndexManager.openIndexWriter(IP, INDEX_CONTEXT, System.currentTimeMillis());
		assertNotNull(indexWriter);
		IndexManager.closeIndexWriter(INDEX_CONTEXT);
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@Test
	public void addStringField() throws Exception {
		// String, String, Document, Store, Index, TermVector
		// IndexableColumn, String
		String stringFieldValue = "string field value";
		IndexManager.addStringField(fieldName, stringFieldValue, document, store, index, termVector);

		// Verify that it not null
		Field field = document.getField(fieldName);
		assertNotNull(field);
		// Verify that the value is the same as the field value string
		assertEquals(stringFieldValue, field.stringValue());

		// Add another field with the same name and
		// verify that the string fields have been merged
		IndexManager.addStringField(fieldName, stringFieldValue, document, store, index, termVector);

		field = document.getField(fieldName);
		assertNotNull(field);
		// Verify that the value is the same as the field value string
		assertEquals(stringFieldValue + " " + stringFieldValue, field.stringValue());
	}

	@Test
	public void addReaderField() throws Exception {
		// String, Document, Store, TermVector, Reader
		// We want to add a reader field to the document
		Reader reader = getReader(Reader.class);
		IndexManager.addReaderField(fieldName, document, store, termVector, reader);

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
		IndexManager.addReaderField(fieldName, document, store, termVector, fieldReader);

		// Verify that it is not null
		Reader finalFieldReader = field.readerValue();
		assertNotNull(finalFieldReader);
	}

	@Test
	public void closeIndexWriter() throws Exception {
		// IndexContext
		IndexWriter indexWriter = IndexManager.openIndexWriter(IP, INDEX_CONTEXT, System.currentTimeMillis());
		assertNotNull(indexWriter);
		IndexManager.closeIndexWriter(INDEX_CONTEXT);
		// assertNull(INDEX_CONTEXT.getIndex().getIndexWriter());
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@Test
	public void getIndexDirectory() {
		String indexDirectoryPath = IndexManager.getIndexDirectory(IP, INDEX_CONTEXT, System.currentTimeMillis());
		logger.info("Index directory : " + new File(indexDirectoryPath).getAbsolutePath());
		assertNotNull(indexDirectoryPath);
	}

	@Test
	public void getIndexDirectoryPathBackup() {
		String indexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(INDEX_CONTEXT);
		logger.info("Index directory path backup : " + indexDirectoryPathBackup);

		when(INDEX_CONTEXT.getIndexDirectoryPathBackup()).thenReturn("./indexes/./backup");
		String newIndexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(INDEX_CONTEXT);
		String expectedIndexDirectoryPathBackup = "./indexes/backup/index";
		assertEquals("Expected path to be : " + expectedIndexDirectoryPathBackup, expectedIndexDirectoryPathBackup,
				newIndexDirectoryPathBackup);

		when(INDEX_CONTEXT.getIndexDirectoryPathBackup()).thenReturn(".\\indexes\\.\\backup");
		newIndexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(INDEX_CONTEXT);
		expectedIndexDirectoryPathBackup = "./indexes/backup/index";
		assertEquals("Expected path to be : " + expectedIndexDirectoryPathBackup, expectedIndexDirectoryPathBackup,
				newIndexDirectoryPathBackup);

		when(INDEX_CONTEXT.getIndexDirectoryPathBackup()).thenReturn(indexDirectoryPathBackup);
	}

	private <T extends Reader> T getReader(Class<T> t) throws Exception {
		T reader = mock(t);
		when(reader.read(any(char[].class))).thenReturn(-1);
		return reader;
	}

}