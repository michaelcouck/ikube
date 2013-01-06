package ikube.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.mock.IndexWriterMock;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.Reader;
import java.util.Date;

import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
@SuppressWarnings("deprecation")
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
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File("./" + IndexManagerTest.class.getSimpleName()), 1);
	}

	@Test
	public void openIndexWriter() throws Exception {
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
		IndexManager.closeIndexWriters(indexContext);
		assertNotNull(indexWriter);
	}

	@Test
	public void addStringField() throws Exception {
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
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
		IndexManager.closeIndexWriters(indexContext);
		assertNotNull(indexWriter);
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
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexFolderOne.getParentFile().getParentFile().getAbsolutePath());
		assertEquals(indexFolderTwo.getParentFile().getName(), latestIndexDirectory.getName());
	}

	@Test
	public void getLatestIndexDirectoryDate() throws Exception {
		File latestIndexDirectory = createIndex(indexContext, "Any kind of data for the index");
		Date latestIndexDirectoryDate = IndexManager.getLatestIndexDirectoryDate(indexContext);
		logger.info("Latest index directory date : " + latestIndexDirectoryDate.getTime());
		assertTrue(latestIndexDirectoryDate.getTime() == Long.parseLong(latestIndexDirectory.getParentFile().getName()));
	}

	@Test
	public void getNumDocsIndexWriter() throws Exception {
		try {
			IndexWriterMock.setIsLocked(Boolean.TRUE);
			Mockit.setUpMocks(IndexWriterMock.class);

			when(fsDirectory.makeLock(anyString())).thenReturn(lock);
			when(indexWriter.numDocs()).thenReturn(Integer.MAX_VALUE);
			when(indexWriter.getDirectory()).thenReturn(fsDirectory);
			when(indexContext.getIndexWriters()).thenReturn(new IndexWriter[] { indexWriter });
			logger.info("Index writer test : " + indexWriter);

			long numDocs = IndexManager.getNumDocsIndexWriter(indexContext);
			logger.info("Num docs : " + numDocs);
			assertEquals(Integer.MAX_VALUE, numDocs);

			IndexWriterMock.setIsLocked(Boolean.FALSE);
			when(indexContext.getIndexWriters()).thenReturn(null);
			when(indexReader.numDocs()).thenReturn(Integer.MIN_VALUE);
			numDocs = IndexManager.getNumDocs(indexContext);
			logger.info("Num docs : " + numDocs);
			assertEquals(-2147483648l, numDocs);
		} finally {
			Mockit.tearDownMocks(IndexWriter.class);
		}
	}

	@Test
	public void getNumDocs() throws Exception {
		logger.info("Index writer test : " + indexWriter);
		when(indexContext.getMultiSearcher()).thenReturn(multiSearcher);
		when(multiSearcher.getSearchables()).thenReturn(searchables);
		when(indexSearcher.getIndexReader()).thenReturn(indexReader);
		when(indexReader.numDocs()).thenReturn(Integer.MAX_VALUE);
		long numDocs = IndexManager.getNumDocs(indexContext);
		logger.info("Num docs : " + numDocs);
		assertEquals(Integer.MAX_VALUE, numDocs);
	}

	@Test
	public void getIndexSize() throws Exception {
		createIndex(indexContext, "the ", "string ", "to add");
		IndexManager.getIndexSize(indexContext);
		Mockito.when(indexContext.getIndexName()).thenReturn("anotherIndexName");
		createIndex(indexContext, "the ", "string ", "to add", "bigger");
		Mockito.when(indexContext.getIndexName()).thenReturn("index");

		long indexSize = IndexManager.getIndexSize(indexContext);
		logger.info("Index size : " + indexSize);
		assertTrue("There must be some size in the index : ", indexSize > 0);
	}

	@Test
	public void openIndexWriterDelta() throws Exception {
		IndexWriter[] indexWriters = IndexManager.openIndexWriterDelta(indexContext);
		assertEquals("There should be one new writers open : ", 1, indexWriters.length);

		// First create several indexes in the same directory
		long time = System.currentTimeMillis();
		String[] ips = { "127.0.0.1", "127.0.0.2", "127.0.0.3" };
		String[] strings = { "The ", "quick ", "brown ", "fox ", "jumped" };
		createIndexes(indexContext, time, ips, strings);
		indexWriters = IndexManager.openIndexWriterDelta(indexContext);
		for (final IndexWriter indexWriter : indexWriters) {
			IndexManager.closeIndexWriter(indexWriter);
		}
		assertEquals("There should be three writers open on the indexes : ", 3, indexWriters.length);
	}

	private <T extends Reader> T getReader(Class<T> t) throws Exception {
		T reader = mock(t);
		when(reader.read(any(char[].class))).thenReturn(-1);
		return reader;
	}

}