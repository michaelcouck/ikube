package ikube.index;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.listener.ListenerManager;
import ikube.model.Event;
import ikube.model.IndexContext;
import ikube.model.Message;
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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;

public class SynchronizationManagerTest extends BaseTest {

	private SynchronizationManager synchronizationManager = ApplicationContextManager.getBean(SynchronizationManager.class);

	@BeforeClass
	public static void beforeClass() {
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}
	}

	@AfterClass
	public static void afterClass() {
		SynchronizationManager synchronizationManager = ApplicationContextManager.getBean(SynchronizationManager.class);
		ListenerManager.removeListener(synchronizationManager);
	}

	@Test
	public void getIndexFiles() throws Exception {
		List<File> indexFiles = synchronizationManager.getIndexFiles();
		logger.info("Files : " + indexFiles);
		assertEquals(0, indexFiles.size());

		String serverIndexDirectoryPath = getServerIndexDirectoryPath(indexContext);
		File indexDirectory = new File(serverIndexDirectoryPath);
		createIndex(indexDirectory);

		indexFiles = synchronizationManager.getIndexFiles();
		logger.info("Files : " + indexFiles);
		assertEquals(3, indexFiles.size());

		FileUtilities.deleteFile(indexDirectory, 1);
	}

	@Test
	public void handleNotification() throws Exception {
		// Event
		final List<Message> messages = new ArrayList<Message>();
		ITopic<Message> topic = Hazelcast.getTopic(IConstants.SYNCHRONIZATION_TOPIC);
		MessageListener<Message> messageListener = new MessageListener<Message>() {
			@Override
			public void onMessage(Message message) {
				logger.info("Message : " + message);
				messages.add(message);
			}
		};
		topic.addMessageListener(messageListener);

		Event event = new Event();
		event.setType(Event.SYNCHRONISE);

		synchronizationManager.handleNotification(event);

		long sleep = 1000;
		Thread.sleep(sleep);
		assertEquals(0, messages.size());

		String serverIndexDirectoryPath = getServerIndexDirectoryPath(indexContext);
		File indexDirectory = new File(serverIndexDirectoryPath);
		createIndex(indexDirectory);

		synchronizationManager.handleNotification(event);

		Thread.sleep(sleep);
		assertEquals(1, messages.size());

		FileUtilities.deleteFile(indexDirectory, 1);
		topic.removeMessageListener(messageListener);
	}

	@Test
	public void onMessage() throws Exception {
		// Message
		String serverIndexDirectoryPath = getServerIndexDirectoryPath(indexContext);
		File indexDirectory = new File(serverIndexDirectoryPath);
		createIndex(indexDirectory);

		File segmentsFile = FileUtilities.findFile(indexDirectory, "segments.gen");

		Message message = new Message();
		message.setFilePath(segmentsFile.getAbsolutePath());
		message.setIp(InetAddress.getLocalHost().getHostAddress());
		synchronizationManager.onMessage(message);

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
		synchronizationManager.writeFile(socket);

		FileUtilities.deleteFile(indexDirectory, 1);
	}

}
