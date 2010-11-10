package ikube.action.synchronize;

import ikube.IConstants;
import ikube.action.BaseActionTest;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @see SocketReaderTest
 * @author Michael Couck
 * @since 07.11.10
 * @version 01.00
 */
@Ignore
public class SocketWriterTest extends BaseActionTest {

	@Test
	public void write() throws Exception {
		// Start the writer listening for requests
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					new SocketWriter().read();
				} catch (Exception e) {
					logger.error("Exception starting the writer listening : ", e);
				}
			}
		});
		thread.start();

		// Create a new index somewhere
		File baseIndexDirectory = FileUtilities.getFile("C:/Temp/writerServer", Boolean.TRUE);
		String filePath = new StringBuilder(baseIndexDirectory.getAbsolutePath()).append(File.separator).append(
				Long.toString(System.currentTimeMillis())).toString();
		File latestIndexDirectory = FileUtilities.getFile(filePath, Boolean.TRUE);
		// Set the server name different from that of the target, this will force the target
		// to accept the index files
		indexContext.setServerName("localhost");
		indexContext.setLatestIndexDirectoryName(latestIndexDirectory.getName());
		File serverIndexDirectory = this.createIndex(latestIndexDirectory, indexContext.getServerName());
		for (File indexFile : serverIndexDirectory.listFiles()) {
			// Write the index context to the writer
			indexContext.setIndexFileName(indexFile.getName());
			Socket socket = new Socket("localhost", IConstants.SOURCE_PORT);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			objectOutputStream.writeObject(indexContext);
		}
		thread.join();
	}

}
