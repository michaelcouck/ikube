package ikube.action.rule;

import ikube.AbstractTest;
import ikube.action.index.IndexManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.UriUtilities;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29.03.2011
 */
public class DirectoryExistsAndNotLockedTest extends AbstractTest {

    private DirectoryExistsAndNotLocked existsAndNotLocked;

    @Before
    public void before() {
        existsAndNotLocked = new DirectoryExistsAndNotLocked();
        when(indexContext.getIndexDirectoryPath()).thenReturn(this.getClass().getSimpleName());
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @After
    public void afterClass() {
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    public void evaluate() throws Exception {
        File indexDirectory = new File(indexContext.getIndexDirectoryPath());
        boolean existsAndNotLocked = this.existsAndNotLocked.evaluate(indexDirectory);
        assertFalse(existsAndNotLocked);

        createIndexFileSystem(indexContext, "Hello world");

        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
        indexDirectory = new File(latestIndexDirectory, UriUtilities.getIp());
        existsAndNotLocked = this.existsAndNotLocked.evaluate(indexDirectory);
        assertTrue(existsAndNotLocked);

        Lock lock = null;
        try {
            lock = getLock(FSDirectory.open(indexDirectory), indexDirectory);
            existsAndNotLocked = this.existsAndNotLocked.evaluate(indexDirectory);
            assertFalse(existsAndNotLocked);
        } finally {
            if (lock != null) {
                lock.release();
            }
        }
    }

}