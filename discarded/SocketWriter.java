package ikube.action.synchronize;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketWriter extends Sockets {

	void read() throws Exception {
		ServerSocket serverSocket = openSocket(IConstants.SOURCE_PORT);
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				logger.info("Incoming message from target : " + socket);
				// We are expecting a true or false from the target
				ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
				IndexContext indexContext = (IndexContext) objectInputStream.readObject();
				logger.info("Index context from target : " + indexContext);
				// Write the file in the index context to the target
				write(indexContext);
			} catch (Exception e) {
				logger.error("Exception reading/writing  from/to the target server : ", e);
			}
		}
	}

	void write(IndexContext indexContext) {
		try {
			File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
			File[] serverIndexDirectories = latestIndexDirectory.listFiles();
			for (File serverIndexDirectory : serverIndexDirectories) {
				File[] indexFiles = serverIndexDirectory.listFiles();
				for (File indexFile : indexFiles) {
					if (indexFile.getName().equals(indexContext.getIndexFileName())) {
						logger.info("Sending target : " + indexContext.getServerName() + ", index file : " + indexFile);
						writeFile(indexContext, indexFile);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception writing files to server : ", e);
		}
	}

	void writeFile(IndexContext indexContext, File indexFile) throws Exception {
		InetAddress host = InetAddress.getByName(indexContext.getServerName());
		Socket socket = null;
		InputStream fileInputStream = null;
		ObjectOutputStream outputStream = null;
		try {
			socket = new Socket(host.getHostName(), IConstants.TARGET_PORT);
			logger.info("Opened socket : " + socket);
			fileInputStream = new FileInputStream(indexFile);
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			// First write the index context
			logger.info("Writing index context : " + indexContext);
			outputStream.writeObject(indexContext);
			outputStream.flush();
			// Then write the file data
			logger.info("Writing file data : " + indexFile);
			writeFile(fileInputStream, outputStream);
		} finally {
			close(fileInputStream);
			close(outputStream);
			close(socket);
		}
	}

	void writeFile(InputStream inputStream, OutputStream outputStream) throws Exception {
		byte[] bytes = new byte[1024];
		int read = -1;
		while ((read = inputStream.read(bytes)) > -1) {
			logger.info("Read bytes : " + read);
			outputStream.write(bytes, 0, read);
		}
		outputStream.flush();
	}

}