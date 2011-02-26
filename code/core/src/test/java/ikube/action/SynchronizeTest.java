package ikube.action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.listener.Event;
import ikube.model.IndexContext;
import ikube.model.SynchronizationMessage;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;

@Ignore
public class SynchronizeTest extends BaseTest {

	private Synchronize synchronize = ApplicationContextManager.getBean(Synchronize.class);

	@BeforeClass
	public static void beforeClass() {
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}
	}

	@Test
	public void getIndexFiles() throws Exception {
		List<File> indexFiles = synchronize.getIndexFiles();
		logger.info("Files : " + indexFiles);
		assertEquals(0, indexFiles.size());

		String serverIndexDirectoryPath = getServerIndexDirectoryPath(indexContext);
		File indexDirectory = new File(serverIndexDirectoryPath);
		createIndex(indexDirectory);

		indexFiles = synchronize.getIndexFiles();
		logger.info("Files : " + indexFiles);
		assertEquals(3, indexFiles.size());

		FileUtilities.deleteFile(indexDirectory, 1);
	}

	@Test
	public void execute() throws Exception {
		// Event
		final List<SynchronizationMessage> synchronizationMessages = new ArrayList<SynchronizationMessage>();
		ITopic<SynchronizationMessage> topic = Hazelcast.getTopic(IConstants.SYNCHRONIZATION_TOPIC);
		MessageListener<SynchronizationMessage> messageListener = new MessageListener<SynchronizationMessage>() {
			@Override
			public void onMessage(SynchronizationMessage synchronizationMessage) {
				logger.info("SynchronizationMessage : " + synchronizationMessage);
				synchronizationMessages.add(synchronizationMessage);
			}
		};
		topic.addMessageListener(messageListener);

		Event event = new Event();
		event.setType(Event.SYNCHRONISE);

		synchronize.execute(indexContext);

		long sleep = 1000;
		Thread.sleep(sleep);
		assertEquals(0, synchronizationMessages.size());

		String serverIndexDirectoryPath = getServerIndexDirectoryPath(indexContext);
		File indexDirectory = new File(serverIndexDirectoryPath);
		createIndex(indexDirectory);

		synchronize.execute(indexContext);

		Thread.sleep(sleep);
		assertEquals(1, synchronizationMessages.size());

		FileUtilities.deleteFile(indexDirectory, 1);
		topic.removeMessageListener(messageListener);
	}

	@Test
	public void onMessage() throws Exception {
		// SynchronizationMessage
		String serverIndexDirectoryPath = getServerIndexDirectoryPath(indexContext);
		File indexDirectory = new File(serverIndexDirectoryPath);
		createIndex(indexDirectory);

		File segmentsFile = FileUtilities.findFile(indexDirectory, "segments.gen");

		SynchronizationMessage synchronizationMessage = new SynchronizationMessage();
		synchronizationMessage.setFilePath(segmentsFile.getAbsolutePath());
		synchronizationMessage.setIp(InetAddress.getLocalHost().getHostAddress());
		synchronize.onMessage(synchronizationMessage);

		FileUtilities.deleteFile(indexDirectory, 1);
	}

	@Test
	public void writeFile() throws Exception {
		// TODO - How can we test this without deploying to different Jvm(s)?
		String serverIndexDirectoryPath = getServerIndexDirectoryPath(indexContext);
		File indexDirectory = new File(serverIndexDirectoryPath);
		createIndex(indexDirectory);

		Socket socket = mock(Socket.class);
		OutputStream outputStream = new ByteArrayOutputStream();
		when(socket.getOutputStream()).thenReturn(outputStream);
		synchronize.writeFile(socket);

		FileUtilities.deleteFile(indexDirectory, 1);
	}

}
