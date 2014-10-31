package ikube.action.rule;

import ikube.AbstractTest;
import ikube.action.index.IndexManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.UriUtilities;
import mockit.Deencapsulation;
import mockit.Mockit;
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
 * @since 14-07-2011
 */
public class IsThisIndexCreatedTest extends AbstractTest {

    private IsThisIndexCreated isThisIndexCreated;

    @Before
    public void before() {
        isThisIndexCreated = new IsThisIndexCreated();
        Deencapsulation.setField(isThisIndexCreated, clusterManager);
        when(indexContext.getIndexDirectoryPath()).thenReturn("./" + this.getClass().getSimpleName());
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
        Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    public void execute() throws Exception {
        boolean isIndexCreated = isThisIndexCreated.evaluate(indexContext);
        assertFalse("This index should not be created yet : ", isIndexCreated);

        // Create an index and lock it
        Lock lock = null;
        createIndexFileSystem(indexContext, "Some data : ");
        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
        File indexDirectory = new File(latestIndexDirectory, UriUtilities.getIp());
        try {
            lock = getLock(FSDirectory.open(indexDirectory), indexDirectory);
            isIndexCreated = isThisIndexCreated.evaluate(indexContext);
            assertTrue("This index should be created : ", isIndexCreated);
        } finally {
            assert lock != null;
            lock.close();
        }

        // Index now unlocked but still exists
        isIndexCreated = isThisIndexCreated.evaluate(indexContext);
        assertTrue("This index should be created : ", isIndexCreated);
    }

}
