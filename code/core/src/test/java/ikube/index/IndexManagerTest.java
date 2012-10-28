package ikube.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
	
	private File indexFolderOne;
	private File indexFolderTwo;
	private File indexFolderThree;

	public IndexManagerTest() {
		super(IndexManagerTest.class);
	}
	
	@Before
	public void before() {
		indexFolderOne = FileUtilities.getFile("./" + IndexManagerTest.class.getSimpleName() + "/1234567889/127.0.0.1", Boolean.TRUE);
		indexFolderTwo = FileUtilities.getFile("./" + IndexManagerTest.class.getSimpleName() + "/1234567891/127.0.0.2", Boolean.TRUE);
		indexFolderThree = FileUtilities.getFile("./" + IndexManagerTest.class.getSimpleName() + "/1234567890/127.0.0.3", Boolean.TRUE);
	}
	
	@After
	public void after() {
		FileUtilities.deleteFile(new File("./" + IndexManagerTest.class.getSimpleName()), 1);
	}

	@Test
	public void openIndexWriter() throws Exception {
		// String, IndexContext, long
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
		assertNotNull(indexWriter);
		IndexManager.closeIndexWriter(indexContext);
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
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
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
		assertNotNull(indexWriter);
		IndexManager.closeIndexWriter(indexContext);
		// assertNull(INDEX_CONTEXT.getIndex().getIndexWriter());
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void getIndexDirectory() {
		String indexDirectoryPath = IndexManager.getIndexDirectory(indexContext, System.currentTimeMillis(), ip);
		logger.info("Index directory : " + new File(indexDirectoryPath).getAbsolutePath());
		assertNotNull(indexDirectoryPath);
	}

	@Test
	public void getIndexDirectoryPathBackup() {
		String indexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);
		logger.info("Index directory path backup : " + indexDirectoryPathBackup);

		when(indexContext.getIndexDirectoryPathBackup()).thenReturn("./indexes/./backup");
		String newIndexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);
		assertTrue(newIndexDirectoryPathBackup.contains("indexes/backup/index"));

		when(indexContext.getIndexDirectoryPathBackup()).thenReturn(".\\indexes\\.\\backup");
		newIndexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);
		assertTrue(newIndexDirectoryPathBackup.contains("indexes/backup/index"));
		assertFalse(newIndexDirectoryPathBackup.contains("\\.\\"));

		when(indexContext.getIndexDirectoryPathBackup()).thenReturn(indexDirectoryPathBackup);
	}
	
	@Test
	public void getLatestIndexDirectoryFileFile() {
		// File, File
		File latest = IndexManager.getLatestIndexDirectory(indexFolderOne.getParentFile().getParentFile(), null);
		logger.info("Latest index directory : " + latest);
		assertEquals(indexFolderTwo.getParentFile(), latest);
		latest = IndexManager.getLatestIndexDirectory(indexFolderTwo.getParentFile().getParentFile(), null);
		assertEquals(indexFolderTwo.getParentFile(), latest);
		latest = IndexManager.getLatestIndexDirectory(indexFolderThree.getParentFile().getParentFile(), null);
		assertEquals(indexFolderTwo.getParentFile(), latest);
		
		createIndex(indexContext, "The data in the index");
		latest = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
		assertTrue(latest != null && latest.exists());
	}

	@Test
	public void getLatestIndexDirectoryString() {
		// String
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexFolderOne.getParentFile().getParentFile().getAbsolutePath());
		assertEquals(indexFolderTwo.getParentFile().getName(), latestIndexDirectory.getName());
	}
	
	
	@Test
	@Ignore
	public void remoteOptimize() {
		File indexDirectory = new File("/media/nas/xfs-one/history/index/wikiHistoryOne/1347197297617/192.168.1.4.8000");
		IndexWriter indexWriter = null;
		try {
			indexWriter = IndexManager.openIndexWriter(indexContext, indexDirectory, false);
			indexWriter.forceMerge(5, true);
		} catch (Exception e) {
			logger.error("Exception optimizing index : ", e);
		}
	}

	private <T extends Reader> T getReader(Class<T> t) throws Exception {
		T reader = mock(t);
		when(reader.read(any(char[].class))).thenReturn(-1);
		return reader;
	}

}