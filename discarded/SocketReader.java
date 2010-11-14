package ikube.action.synchronize;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

public class SocketReader extends Sockets {

	Logger logger = Logger.getLogger(this.getClass());

	void read() {
		ServerSocket serverSocket = openSocket(IConstants.TARGET_PORT);
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				logger.info("Incoming request from source : " + socket);
				// We expect either an index context or a file data stream
				ObjectInputStream objectInputStream = null;
				try {
					// Get the index context from the remote server, and check
					// if we have the file that they are wanting to send
					objectInputStream = new ObjectInputStream(socket.getInputStream());
					IndexContext indexContext = (IndexContext) objectInputStream.readObject();
					logger.info("Index context from source : " + indexContext);
					// Create the file
					File indexFile = getIndexFile(indexContext);
					logger.info("Writing index file : " + indexFile.length() + ", " + indexFile);
					// The next stream from the socket will be the data from the index file it's self
					writeFile(objectInputStream, indexFile);
				} catch (Exception e) {
					logger.error("Exception writing the index file : ", e);
				} finally {
					close(objectInputStream);
					close(socket);
				}
			} catch (Exception e) {
				logger.error("Exception listening on the prot : " + IConstants.SOURCE_PORT, e);
			}
		}
	}

	void writeFile(InputStream inputStream, File indexFile) throws Exception {
		OutputStream outputStream = null;
		try {
			byte[] bytes = new byte[1024];
			int read = -1;
			outputStream = new FileOutputStream(indexFile);
			// Should we create a lock on this directory while we write? Lucene lock?
			while ((read = inputStream.read(bytes)) > -1) {
				// Write the bytes to the file
				logger.info("Read : " + read + " bytes of index file : ");
				outputStream.write(bytes, 0, read);
			}
		} finally {
			close(outputStream);
		}
	}

	File getIndexFile(IndexContext indexContext) throws Exception {
		logger.info("Index context : " + indexContext);
		// If we have this file then respond false
		File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		String filePath = new StringBuilder(baseIndexDirectory.getAbsolutePath()).append(File.separator).append(
				indexContext.getLatestIndexDirectoryName()).toString();
		File latestIndexDirectory = FileUtilities.getFile(filePath, Boolean.TRUE);
		filePath = new StringBuilder(latestIndexDirectory.getAbsolutePath()).append(File.separator).append(indexContext.getServerName())
				.toString();
		File serverIndexDirectory = FileUtilities.getFile(filePath, Boolean.TRUE);
		filePath = new StringBuilder(serverIndexDirectory.getAbsolutePath()).append(File.separator).append(indexContext.getIndexFileName())
				.toString();
		File indexFile = FileUtilities.getFile(filePath, Boolean.FALSE); // new File(serverIndexDirectory, indexContext.getIndexFileName());
		logger.info("Index file : " + indexFile);
		return indexFile;
	}

}
