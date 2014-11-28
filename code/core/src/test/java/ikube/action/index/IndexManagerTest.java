package ikube.action.index;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.Close;
import ikube.action.Open;
import ikube.mock.IndexWriterMock;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;
import mockit.Mockit;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.Reader;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 12-10-2010
 */
@SuppressWarnings("deprecation")
public class IndexManagerTest extends AbstractTest {

    private File indexFolderTwo;
    private Indexable indexable;
    private String fieldName = "fieldName";

    private Document document = new Document();

    @Before
    public void before() {
        when(indexContext.getIndexDirectoryPath()).thenReturn(this.getClass().getSimpleName());
        indexable = new Indexable() {
        };
        FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()));
        getFile(indexContext.getIndexDirectoryPath(), indexContext.getName(), "/1234567889/127.0.0.1");
        indexFolderTwo = getFile(indexContext.getIndexDirectoryPath(), indexContext.getName(), "/1234567891/127.0.0.2");
        getFile(indexContext.getIndexDirectoryPath(), indexContext.getName(), "/1234567890/127.0.0.3");
        THREAD.sleep(1000);
    }

    private File getFile(final String base, final String folder, final String name) {
        File file = new File(base, folder + IConstants.SEP + name);
        String folderPath = FILE.cleanFilePath(file.getAbsolutePath());
        return FILE.getFile(folderPath, Boolean.TRUE);
    }

    @After
    public void after() {
        FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()));
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
        IndexManager.addStringField(fieldName, stringFieldValue, indexable, document);

        // Verify that it not null
        IndexableField field = document.getField(fieldName);
        assertNotNull(field);
        // Verify that the value is the same as the field value string
        assertEquals(stringFieldValue, field.stringValue());

        // Add another field with the same name and
        // verify that the string fields have been merged
        IndexManager.addStringField(fieldName, stringFieldValue, indexable, document);

        field = document.getField(fieldName);
        assertNotNull(field);
        // Verify that the value is the same as the field value string
        assertEquals(stringFieldValue + " " + stringFieldValue, field.stringValue());
    }

    @Test
    public void addNumericField() {
        String floatFieldValue = "123.456";
        IndexManager.addNumericField(fieldName, floatFieldValue, document, Boolean.TRUE, 1.0f);

        IndexableField field = document.getField(fieldName);
        assertNotNull(field);
        // Verify that the value is the same as the field value string
        assertEquals(floatFieldValue, field.stringValue());
    }

    @Test
    public void addReaderField() throws Exception {
        // We want to add a reader field to the document
        Reader reader = getReader(Reader.class);
        IndexManager.addReaderField(fieldName, document, reader, Boolean.TRUE, 1.0f);

        // Verify that it not null
        Field field = (Field) document.getField(fieldName);
        assertNotNull(field);
        document.removeField(fieldName);
        field = (Field) document.getField(fieldName);
        assertNull(field);

        // Now we want to add a reader field that will be merged
        Reader fieldReader = getReader(Reader.class);
        field = new Field(fieldName, fieldReader);
        document.add(field);
        IndexManager.addReaderField(fieldName, document, fieldReader, Boolean.TRUE, 1.0f);

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
        assertNotNull(indexDirectoryPath);
    }

    @Test
    public void getIndexDirectoryPathBackup() {
        String indexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);

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
    public void getLatestIndexDirectory() throws Exception {
        File base = new File(indexContext.getIndexDirectoryPath(), indexContext.getName());
        File latest = IndexManager.getLatestIndexDirectory(base, null);
        logger.info("Latest : " + latest.getAbsolutePath());
        assertEquals(indexFolderTwo.getParentFile().getAbsolutePath(), FILE.cleanFilePath(latest.getAbsolutePath()));

        Date latestIndexDirectoryDate = IndexManager.getLatestIndexDirectoryDate(indexContext);
        logger.info("Latest date : " + latestIndexDirectoryDate.getTime() + ", " + latest.getName());
        assertTrue(latestIndexDirectoryDate.getTime() == Long.parseLong(latest.getName()));

        createIndexFileSystem(indexContext, "The data in the index");
        latest = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
        logger.info("Latest : " + latest.getAbsolutePath());
        assertTrue(latest != null && latest.exists());
        assertNotSame(indexFolderTwo.getParentFile(), latest);

        when(indexContext.getIndexName()).thenReturn("different-index-context");
        File latestIndexDirectory = createIndexFileSystem(indexContext, "The data in the index");
        latestIndexDirectoryDate = IndexManager.getLatestIndexDirectoryDate(indexContext);
        logger.info("Latest index directory : " + latestIndexDirectory + ", " + latestIndexDirectoryDate.getTime());
        assertEquals(Long.parseLong(latestIndexDirectory.getParentFile().getName()), latestIndexDirectoryDate.getTime());

        latestIndexDirectory = createIndexFileSystem(indexContext, "The data in the index");
        assertNotSame(Long.parseLong(latestIndexDirectory.getParentFile().getName()), latestIndexDirectoryDate.getTime());

        latestIndexDirectoryDate = IndexManager.getLatestIndexDirectoryDate(indexContext);
        logger.info("Latest index directory : " + latestIndexDirectory  + ", " + latestIndexDirectoryDate.getTime());
        assertEquals(Long.parseLong(latestIndexDirectory.getParentFile().getName()), latestIndexDirectoryDate.getTime());
    }

    @Test
    public void getNumDocsIndexWriter() throws Exception {
        try {
            IndexWriterMock.setIsLocked(Boolean.TRUE);
            Mockit.setUpMocks(IndexWriterMock.class);

            when(fsDirectory.makeLock(anyString())).thenReturn(lock);
            when(indexWriter.numDocs()).thenReturn(Integer.MAX_VALUE);
            when(indexWriter.getDirectory()).thenReturn(fsDirectory);
            when(indexContext.getIndexWriters()).thenReturn(new IndexWriter[]{indexWriter});

            long numDocs = IndexManager.getNumDocsForIndexWriters(indexContext);
            assertEquals(Integer.MAX_VALUE, numDocs);

            IndexWriterMock.setIsLocked(Boolean.FALSE);
            when(indexContext.getIndexWriters()).thenReturn(null);
            when(indexReader.numDocs()).thenReturn(Integer.MIN_VALUE);
            numDocs = IndexManager.getNumDocsForIndexWriters(indexContext);
            assertEquals(0, numDocs);
        } finally {
            Mockit.tearDownMocks(IndexWriterMock.class);
        }
    }

    @Test
    public void getNumDocs() throws Exception {
        when(indexContext.getMultiSearcher()).thenReturn(multiSearcher);
        when(indexContext.getIndexWriters()).thenReturn(null);
        when(multiSearcher.getIndexReader()).thenReturn(indexReader);
        when(indexSearcher.getIndexReader()).thenReturn(indexReader);
        when(indexReader.numDocs()).thenReturn(Integer.MAX_VALUE);
        long numDocs = IndexManager.getNumDocsForIndexSearchers(indexContext);
        assertEquals(Integer.MAX_VALUE, numDocs);
    }

    @Test
    public void getIndexSize() throws Exception {
        IndexContext indexContext = new IndexContext();
        indexContext.setName("index");
        indexContext.setChildren(indexables);
        indexContext.setIndexDirectoryPath(indexDirectoryPath);
        indexContext.setBufferedDocs(100);
        indexContext.setBufferSize(128);
        indexContext.setMergeFactor(100);

        createIndexFileSystem(indexContext, "the ", "string ", "to add");
        IndexManager.getIndexSize(indexContext);
        createIndexFileSystem(indexContext, "the ", "string ", "to add", "bigger");

        long indexSize = IndexManager.getIndexSize(indexContext);
        assertTrue("There must be some size in the index : ", indexSize > 0);

        new Open().execute(indexContext);
        long numDocs = IndexManager.getNumDocsForIndexSearchers(indexContext);
        assertEquals(4, numDocs);
        new Close().execute(indexContext);

        IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
        indexContext.setIndexWriters(indexWriter);
        addDocuments(indexWriter, IConstants.CONTENTS, "some", "index", "documents");

        numDocs = IndexManager.getNumDocsForIndexWriters(indexContext);
        assertEquals(3, numDocs);
    }

    @Test
    public void openIndexWriterDelta() throws Exception {
        IndexWriter[] indexWriters = null;
        try {
            FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()));
            indexWriters = IndexManager.openIndexWriterDelta(indexContext);
            assertEquals("There should be one new writers open : ", 1, indexWriters.length);

            // First create several indexes in the same directory
            long time = System.currentTimeMillis();
            String[] ips = {"127.0.0.1", "127.0.0.2", "127.0.0.3"};
            String[] strings = {"The ", "quick ", "brown ", "fox ", "jumped"};
            createIndexesFileSystem(indexContext, time, ips, strings);
            indexWriters = IndexManager.openIndexWriterDelta(indexContext);
            assertEquals("There should be three writers open on the indexes : ", 3, indexWriters.length);
        } finally {
            if (indexWriters != null) {
                for (final IndexWriter indexWriter : indexWriters) {
                    IndexManager.closeIndexWriter(indexWriter);
                }
            }
        }
    }

    private <T extends Reader> T getReader(Class<T> t) throws Exception {
        T reader = mock(t);
        when(reader.read(any(char[].class))).thenReturn(-1);
        return reader;
    }

}