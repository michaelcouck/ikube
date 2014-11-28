package ikube.action;

import ikube.AbstractTest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.toolkit.FILE;
import mockit.Deencapsulation;
import mockit.Mockit;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 17-04-2011
 */
public class ValidatorTest extends AbstractTest {

    private Validator validator;

    @Before
    public void before() {
        Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
        validator = spy(new Validator());
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                logger.info("Message sent : " + invocation);
                return null;
            }
        }).when(validator).sendNotification(anyString(), anyString());
        Deencapsulation.setField(validator, clusterManager);
        when(indexContext.getIndexDirectoryPath()).thenReturn("./" + this.getClass().getSimpleName());
        FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @After
    public void after() {
        Mockit.tearDownMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
        FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    public void validate() throws Exception {
        boolean result = validator.execute(indexContext);
        assertFalse("There are no indexes created : ", result);
        // There should be a mail sent because there are no indexes created
        // There should also be a mail sent because the searchable is not opened
        // There should be a mail sent because there is no backup for this index
        int invocations = 3;
        verify(validator, Mockito.times(invocations)).sendNotification(anyString(), anyString());

        File latestIndexDirectory = createIndexFileSystem(indexContext, "a little sentence");
        result = validator.execute(indexContext);
        assertFalse("There is an index created but no backup : ", result);
        // There should be no mail sent because there is an index generated
        // There should be a mail sent because there is no backup for this index
        invocations++;
        verify(validator, Mockito.times(invocations)).sendNotification(anyString(), anyString());

        Backup backup = new Backup();
        Deencapsulation.setField(backup, clusterManager);
        backup.execute(indexContext);
        result = validator.execute(indexContext);
        assertTrue("There is an index created : ", result);

        Directory directory = FSDirectory.open(latestIndexDirectory);
        Lock lock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
        lock.obtain(1000);
        result = validator.execute(indexContext);
        assertFalse("The index is locked : ", result);
        // There should be a message sent to notify that there is an index being
        // generated and that the current index is not current
        lock.close();
        directory.clearLock(IndexWriter.WRITE_LOCK_NAME);
        invocations++;
        invocations++;
        verify(validator, Mockito.times(invocations)).sendNotification(anyString(), anyString());

        // Delete one file in the index and there should be an exception
        List<File> files = FILE.findFilesRecursively(latestIndexDirectory, new ArrayList<File>(), "segments");
        for (File file : files) {
            FILE.deleteFile(file, 1);
        }
        result = validator.execute(indexContext);
        assertFalse("The index is corrupt : ", result);
        // There should be a mail sent because the index is corrupt
        invocations += 2;
        verify(validator, Mockito.times(invocations)).sendNotification(anyString(), anyString());

        validator.execute(indexContext);
        // There should be another mail sent
        invocations += 2;
        verify(validator, Mockito.times(invocations)).sendNotification(anyString(), anyString());
    }

}