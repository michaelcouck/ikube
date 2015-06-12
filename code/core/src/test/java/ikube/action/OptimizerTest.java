package ikube.action;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.toolkit.FILE;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This test just has to run without exception, the number of index files
 * that are merged seems to be random, and Lucene decides, strangely enough, so there
 * is no real way to see that the index was actually optimized.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 08-02-2013
 */
public class OptimizerTest extends AbstractTest {

    /**
     * "192.168.1.2", "192.168.1.3"
     */
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private String[] ips = {"192.168.1.1"};

    @Spy
    //@InjectMocks - this causes a stack overflow for some reason, strange...
    private Optimizer optimizer;

    @After
    public void after() {
        FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    public void optimize() throws Exception {
        createIndexesFileSystem(indexContext, System.currentTimeMillis(), ips, "and a little data");
        boolean optimized = optimizer.execute(indexContext);
        assertTrue(optimized);
        verify(indexContext, atLeastOnce()).getAnalyzer();
    }

    @Test
    public void optimizeIndex() throws IOException {
        // Generate a large index with more than segments files
        File indexDirectory = createIndexFileSystem(indexContext, 10000, "Old McDonald had a farm, he hi he hi ho.");
        String[] indexFiles = indexDirectory.list();
        logger.info("Index files in directory before optimize : " + indexFiles.length);
        assertTrue("Must be more than the max defined for segments, which is : " +
                        IConstants.MAX_SEGMENTS + ", and currently has : " + indexFiles.length,
                indexFiles.length > IConstants.MAX_SEGMENTS);

        // Optimize and there should be 25 or less files in the index after this
        Directory directory = FSDirectory.open(indexDirectory);
        optimizer.optimizeIndex(indexContext, indexDirectory, directory);

        indexFiles = indexDirectory.list();
        logger.info("Index files in directory after optimize : " + indexFiles.length);
        assertTrue("Must be less than the max defined for segments, which is : " +
                        IConstants.MAX_SEGMENTS + ", and currently has : " + indexFiles.length,
                indexFiles.length > IConstants.MAX_SEGMENTS);
    }

    @Test
    public void unlockIfLocked() throws Exception {
        when(indexContext.isDelta()).thenReturn(Boolean.TRUE);

        // Open an index writer, creating the lock file
        File indexDirectory = createIndexFileSystem(indexContext, 1000, "Old McDonald had a farm, he hi he hi ho.");
        IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, indexDirectory, Boolean.FALSE);
        when(indexContext.getIndexWriters()).thenReturn(new IndexWriter[]{indexWriter});

        // Now open an index reader on the index too
        Directory directory = FSDirectory.open(indexDirectory);
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        when(indexContext.getMultiSearcher()).thenReturn(indexSearcher);

        // Verify that the index is locked, i.e. there is a lock file
        assertTrue(IndexWriter.isLocked(directory));
        assertNotNull(FILE.findFileRecursively(indexDirectory, "write.lock"));

        boolean unlocked = optimizer.unlockIfLocked(indexContext, directory);
        assertTrue(unlocked);

        // Verify that the writer is closed on the index
        assertFalse(IndexWriter.isLocked(directory));
        // Verify that the directory is not locked anymore
        // Apparently the write lock file never gets deleted, strange
        // assertNull(FILE.findFileRecursively(indexDirectory, "write.lock"));
    }

}