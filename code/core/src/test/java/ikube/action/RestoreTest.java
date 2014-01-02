package ikube.action;

import ikube.AbstractTest;
import ikube.action.index.IndexManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.toolkit.FileUtilities;
import mockit.Deencapsulation;
import mockit.Mockit;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests the restore process that copies an index from the backup directory to the index directory and sets the time stamp ahead of the max
 * age to give the server a time to recover before starting a new index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 17.04.11
 */
public class RestoreTest extends AbstractTest {

    private Restore restore;

    @Before
    public void before() throws Exception {
        restore = new Restore();
        Deencapsulation.setField(restore, clusterManager);
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPathBackup()), 1);
    }

    @After
    public void after() throws Exception {
        // Mockit.tearDownMocks();
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPathBackup()), 1);
    }

    @Test
    public void execute() throws Exception {
        // Create an index in the normal directory
        File latestIndexDirectory = createIndexFileSystem(indexContext, "the original text fragment");

        // Create an index in the backup directory
        when(indexContext.getIndexDirectoryPath()).thenReturn(indexDirectoryPathBackup);
        createIndexFileSystem(indexContext, "a little text");
        when(indexContext.getIndexDirectoryPath()).thenReturn(indexDirectoryPath);

        // Corrupt the index
        FileUtilities.deleteFiles(latestIndexDirectory, "segments");

        // Run the restore
        Mockit.setUpMocks(ApplicationContextManagerMock.class);
        restore.execute(indexContext);
        // Mockit.tearDownMocks();

        indexExists();

        // Delete the index and restore it from the backup
        FileUtilities.deleteFile(latestIndexDirectory, 1);
        assertFalse("The index directory should be deleted : ", latestIndexDirectory.exists());

        restore.execute(indexContext);
        indexExists();
    }

    private void indexExists() throws Exception {
        // Check that the index is restored
        String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
        File latestServerIndexDirectory = new File(latestIndexDirectory, ip);
        Directory directory = null;
        try {
            directory = FSDirectory.open(latestServerIndexDirectory);
            assertTrue(DirectoryReader.indexExists(directory));
        } finally {
            if (directory != null) {
                directory.close();
            }
        }
    }

}