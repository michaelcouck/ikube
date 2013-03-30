package ikube.cluster.listener.hzc;

import static org.junit.Assert.assertFalse;
import ikube.ATest;
import ikube.action.index.IndexManager;
import ikube.scheduling.listener.Event;
import ikube.toolkit.FileUtilities;

import java.io.File;

import mockit.Deencapsulation;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.hazelcast.core.Message;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15.12.12
 */
public class DeleteListenerTest extends ATest {

	private DeleteListener deleteListener;

	public DeleteListenerTest() {
		super(DeleteListenerTest.class);
	}

	@Before
	public void before() {
		deleteListener = new DeleteListener();
		Deencapsulation.setField(deleteListener, monitorService);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPathBackup()), 1);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void onMessage() throws Exception {
		Message<Object> message = Mockito.mock(Message.class);
		Event event = new Event();
		event.setType(Event.DELETE_INDEX);
		event.setObject(indexContext.getIndexName());
		Mockito.when(message.getMessageObject()).thenReturn(event);

		Mockito.when(monitorService.getIndexContext(Mockito.anyString())).thenReturn(indexContext);
		File indexDirectory = createIndex(indexContext, "and some", "data to add", "to the index");
		String indexDirectoryBackupPath = IndexManager.getIndexDirectoryPath(indexContext);
		FileUtils.copyDirectory(indexDirectory, new File(indexDirectoryBackupPath));

		logger.info("Index directory to delete : ");
		deleteListener.onMessage(message);
		assertFalse(indexDirectory.exists());
	}

}
