package ikube.cluster.listener.hzc;

import com.hazelcast.core.Message;
import ikube.AbstractTest;
import ikube.action.index.IndexManager;
import ikube.scheduling.schedule.Event;
import ikube.toolkit.FileUtilities;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.io.File;

import static org.junit.Assert.assertFalse;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-12-2012
 */
public class DeleteListenerTest extends AbstractTest {

    @Mock
    private Message<Object> message;
    @Spy
    @InjectMocks
    private DeleteListener deleteListener;

    @After
    public void after() {
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPathBackup()), 1);
    }

    @Test
    public void onMessage() throws Exception {
        Event event = new Event();
        event.setType(Event.DELETE_INDEX);
        event.setObject(indexContext.getIndexName());
        Mockito.when(message.getMessageObject()).thenReturn(event);

        Mockito.when(monitorService.getIndexContext(Mockito.anyString())).thenReturn(indexContext);
        File indexDirectory = createIndexFileSystem(indexContext, "and some", "data to add", "to the index");
        String indexDirectoryBackupPath = IndexManager.getIndexDirectoryPath(indexContext);
        FileUtils.copyDirectory(indexDirectory, new File(indexDirectoryBackupPath));

        logger.info("Index directory to delete : ");
        deleteListener.onMessage(message);
        assertFalse(indexDirectory.exists());
    }

}