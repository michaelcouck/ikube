package ikube.action;

import ikube.AbstractTest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.toolkit.FileUtilities;
import mockit.Deencapsulation;
import mockit.Mockit;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public class DeleteTest extends AbstractTest {

    private Delete delete;

    @Before
    public void before() {
        delete = new Delete();
        Deencapsulation.setField(delete, clusterManager);

        Mockit.setUpMocks(ApplicationContextManagerMock.class);
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @After
    public void after() throws Exception {
        Mockit.tearDownMocks(ApplicationContextManagerMock.class);
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void execute() throws Exception {
        File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
        FileUtilities.deleteFile(baseIndexDirectory, 1);
        baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
        assertTrue("We should start with no directories : ", baseIndexDirectory.exists());

        // 1) No indexes so far, nothing to delete
        boolean deleted = delete.execute(indexContext);
        assertFalse("There are not indexes to delete : ", deleted);

        File latestIndexDirectory = createIndexFileSystem(indexContext, "whatever");
        assertTrue("Server index directory created : ", latestIndexDirectory.exists());

        // 2) Only one directory so nothing to delete
        deleted = delete.execute(indexContext);
        assertFalse("The index should not have been deleted : ", deleted);
        assertEquals("There should be only one index : ", 1, latestIndexDirectory.getParentFile().listFiles().length);

        latestIndexDirectory = createIndexFileSystem(indexContext, "some more whatever");
        assertEquals(2, latestIndexDirectory.getParentFile().getParentFile().listFiles().length);
        // 3) Two directories so both should stay
        deleted = delete.execute(indexContext);
        assertFalse(deleted);
        assertEquals(2, latestIndexDirectory.getParentFile().getParentFile().listFiles().length);

        latestIndexDirectory = createIndexFileSystem(indexContext, "Tired of this?");
        assertEquals(3, latestIndexDirectory.getParentFile().getParentFile().listFiles().length);

        Directory directory = FSDirectory.open(latestIndexDirectory);
        Lock lock = getLock(directory, latestIndexDirectory);

        // 4) Three directories, one locked there should be two left
        deleted = delete.execute(indexContext);
        assertTrue(deleted);
        assertEquals(2, latestIndexDirectory.getParentFile().getParentFile().listFiles().length);

        lock.close();
        directory.clearLock(IndexWriter.WRITE_LOCK_NAME);

        latestIndexDirectory = createIndexFileSystem(indexContext, "some strings");
        assertEquals(3, latestIndexDirectory.getParentFile().getParentFile().listFiles().length);

        directory = FSDirectory.open(latestIndexDirectory);
        lock = getLock(directory, latestIndexDirectory);
        assertTrue(IndexWriter.isLocked(directory));

        // 5) Three directories, one locked, one should be gone
        deleted = delete.execute(indexContext);
        assertTrue(deleted);
        assertEquals(2, latestIndexDirectory.getParentFile().getParentFile().listFiles().length);

        lock.close();
        directory.clearLock(IndexWriter.WRITE_LOCK_NAME);

        FileUtilities.deleteFile(latestIndexDirectory, 1);

        assertFalse(latestIndexDirectory.exists());
    }

}