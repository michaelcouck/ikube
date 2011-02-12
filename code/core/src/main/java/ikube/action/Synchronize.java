package ikube.action;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.SynchronizationMessage;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;

/**
 * TODO - This class can be re-written. Instead of having the port implementation there can be a direct 
 * read from the remote server file system and a write to the local file system. This requires that the servers
 * are on machines and users that have access to each others file systems but reduces the complexity of this
 * process considerably, which doesn't work anyway because the file write process for > 10 gig file can take 
 * a while and there are synchronization issues.
 * 
 * TODO - re-test this class, it doesn't work in a cluster.
 * 
 * @author Michael Couck
 * @since 31.12.10
 * @version 01.00
 */
public class Synchronize extends Action implements MessageListener<SynchronizationMessage> {

	/** The port that the server socket was opened on. */
	private int port;
	/** The file currently being sent. */
	private File currentFile;
	/** The index of the file in the set of index files. */
	private int index = 0;
	/** The size of the chunks of data. */
	private int chunk = 1024 * 1000;

	public Synchronize() {
		// We need to listen to the topic for messages that other servers
		// have published some index files
		ITopic<SynchronizationMessage> topic = Hazelcast.getTopic(IConstants.SYNCHRONIZATION_TOPIC);
		topic.addMessageListener(this);
		new Thread(new Runnable() {
			public void run() {
				// Open a socket on the a synchronization port. We start with
				// the default port and iterate through the ports until we find one that
				// is available
				ServerSocket serverSocket = getServerSocket();
				Socket socket = null;
				while (serverSocket != null) {
					try {
						// Wait for takers to access the current file
						logger.info("Waiting for clients : ");
						socket = serverSocket.accept();
						logger.info("Got client : " + socket + ", " + socket.getRemoteSocketAddress());
						// Write the index file to the output stream
						writeFile(socket);
					} catch (Exception e) {
						logger.error("Exception writing file to client : ", e);
					} finally {
						close(socket);
					}
				}
			}
		}).start();
	}

	@Override
	public Boolean execute(IndexContext indexContext) {
		if (getClusterManager().anyWorking()) {
			logger.info("Servers working : ");
			return Boolean.FALSE;
		}

		// Check to see if there are any new indexes that are finished, i.e. not locked
		List<File> files = getIndexFiles();
		// Publish a message to the cluster with the name
		// of the file that we want to send to each
		if (files.size() == 0) {
			logger.info("No files to distribute yet : " + files);
			return Boolean.FALSE;
		}
		try {
			// This is the file we will publish
			if (index >= files.size()) {
				index = 0;
			}
			currentFile = files.get(index++);
			// logger.info("Publishing file : " + currentFile);
			SynchronizationMessage synchronizationMessage = new SynchronizationMessage();
			synchronizationMessage.setIp(InetAddress.getLocalHost().getHostAddress());
			synchronizationMessage.setPort(port);
			synchronizationMessage.setFilePath(currentFile.getAbsolutePath());
			synchronizationMessage.setFileLength(currentFile.length());
			// logger.info("Publishing message : " + synchronizationMessage);
			Hazelcast.getTopic(IConstants.SYNCHRONIZATION_TOPIC).publish(synchronizationMessage);
		} catch (Exception e) {
			logger.error("Exception publishing files to clients : " + currentFile, e);
		}

		return Boolean.TRUE;
	}

	protected void writeFile(Socket socket) {
		FileInputStream fileInputStream = null;
		OutputStream outputStream = null;
		if (getClusterManager().getServer().isWorking()) {
			return;
		}
		try {
			getClusterManager().setWorking(IConstants.SYNCHRONIZATION, IConstants.SYNCHRONIZATION, Boolean.TRUE);
			boolean fileExists = currentFile != null && currentFile.exists();
			if (fileExists) {
				logger.info("File exists : " + fileExists + ", " + currentFile);
				fileInputStream = new FileInputStream(currentFile);
				outputStream = socket.getOutputStream();
				byte[] bytes = new byte[chunk];
				int read = -1;
				while ((read = fileInputStream.read(bytes)) > -1) {
					outputStream.write(bytes, 0, read);
				}
			}
		} catch (Exception e) {
			logger.error("Exception writing index file to client : ", e);
		} finally {
			close(outputStream);
			close(fileInputStream);
			getClusterManager().setWorking(IConstants.SYNCHRONIZATION, IConstants.SYNCHRONIZATION, Boolean.FALSE);
		}
	}

	protected List<File> getIndexFiles() {
		List<File> files = new ArrayList<File>();
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (String indexContextName : indexContexts.keySet()) {
			IndexContext indexContext = indexContexts.get(indexContextName);
			try {
				String indexDirectoryPath = indexContext.getIndexDirectoryPath() + File.separator + indexContext.getIndexName();
				File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexDirectoryPath);
				logger.info("Latest index directory : " + latestIndexDirectory);
				if (latestIndexDirectory == null) {
					continue;
				}
				File[] ipDirectories = latestIndexDirectory.listFiles();
				for (File ipDirectory : ipDirectories) {
					Directory directory = FSDirectory.open(ipDirectory);
					boolean locked = IndexWriter.isLocked(directory);
					directory.close();
					if (locked) {
						logger.info("Directory locked : " + ipDirectory);
						continue;
					}
					File[] indexFiles = ipDirectory.listFiles();
					if (indexFiles == null || indexFiles.length == 0) {
						continue;
					}
					files.addAll(Arrays.asList(indexFiles));
				}
			} catch (Exception e) {
				logger.error("Exception accessing the file for context : " + indexContext, e);
			}
		}
		return files;
	}

	protected ServerSocket getServerSocket() {
		ServerSocket serverSocket = null;
		port = IConstants.SYNCHRONIZATION_PORT;
		while (true) {
			try {
				logger.info("Trying port : " + port);
				serverSocket = new ServerSocket(port);
				logger.info("Opened synchronization socket : " + serverSocket);
				return serverSocket;
			} catch (Exception e) {
				logger.error("Exception opening a server socket : " + IConstants.SYNCHRONIZATION_PORT, e);
				port++;
				if (port >= IConstants.MAX_SYNCHRONIZATION_PORT) {
					logger.warn("Couldn't find a port available for synchronization : " + port);
					return null;
				}
			}
		}
	}

	@Override
	public void onMessage(SynchronizationMessage synchronizationMessage) {
		// Check that there are no servers working
		if (getClusterManager().anyWorking() || getClusterManager().getServer().isWorking()) {
			logger.info("Servers working : " + getClusterManager().getServers());
			return;
		}
		Directory directory = null;
		Lock directoryLock = null;
		Socket socket = null;
		InputStream inputStream = null;
		FileOutputStream fileOutputStream = null;
		File file = null;
		boolean written = Boolean.TRUE;
		try {
			// Lock the cluster, i.e. set the server working
			getClusterManager().setWorking(IConstants.SYNCHRONIZATION, IConstants.SYNCHRONIZATION, Boolean.TRUE);
			// Check if we have this file
			String filePath = synchronizationMessage.getFilePath();
			String[] parts = StringUtils.tokenizeToStringArray(filePath, "\\/", Boolean.TRUE, Boolean.TRUE);

			int index = parts.length;
			String fileName = parts[--index];
			String ipFolderName = parts[--index];
			String timeFolderName = parts[--index];
			String contextFolderName = parts[--index];

			IndexContext indexContext = null;
			Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
			for (String indexContextName : indexContexts.keySet()) {
				IndexContext mapIndexContext = indexContexts.get(indexContextName);
				if (!mapIndexContext.getIndexName().equals(contextFolderName)) {
					continue;
				}
				indexContext = mapIndexContext;
				break;
			}

			if (indexContext == null) {
				logger.info("Couldn't find index context for : " + filePath);
				return;
			}

			boolean exists = Boolean.FALSE;
			File baseIndexDirectory = new File(indexContext.getIndexDirectoryPath() + File.separator + indexContext.getIndexName());
			String[] patterns = new String[] { fileName };
			List<File> foundFiles = FileUtilities.findFilesRecursively(baseIndexDirectory, patterns, new ArrayList<File>());
			// Iterate through the files with this name and see if they are
			// from the same time index, and the same context and the same
			// ip address folder
			for (File foundFile : foundFiles) {
				String foundFilePath = foundFile.getAbsolutePath();
				if (foundFilePath.contains(ipFolderName) && foundFilePath.contains(timeFolderName)
						&& foundFilePath.contains(contextFolderName)) {
					// Got this file then
					exists = Boolean.TRUE;
					break;
				}
			}

			if (exists) {
				// Either this is our own message or we have got this file previously
				return;
			}

			// Build the file from the path on this machine
			String indexDirectoryPath = IndexManager.getIndexDirectory(ipFolderName, indexContext, Long.parseLong(timeFolderName));

			StringBuilder builder = new StringBuilder();
			builder.append(indexDirectoryPath);
			builder.append(File.separator);
			builder.append(fileName);

			file = FileUtilities.getFile(builder.toString(), Boolean.FALSE);

			logger.info("Writing remote file to : " + file);
			// Lock the index directory
			directory = FSDirectory.open(file.getParentFile());
			directoryLock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
			boolean gotLock = directoryLock.obtain(IndexWriter.WRITE_LOCK_TIMEOUT);
			if (!gotLock) {
				logger.warn("Couldn't get lock to write index file : " + file);
				return;
			}

			String ip = synchronizationMessage.getIp();
			Integer port = synchronizationMessage.getPort();
			// Open a socket to the publishing server
			socket = new Socket(ip, port);
			// Get the file output stream to write the data to
			fileOutputStream = new FileOutputStream(file);
			inputStream = socket.getInputStream();
			byte[] bytes = new byte[chunk];
			int read = -1;
			// Write the file contents from the publisher to the file system
			while ((read = inputStream.read(bytes)) > -1) {
				fileOutputStream.write(bytes, 0, read);
			}
			// Verify that the file is the same length as the one sent
			long fileLength = file.length();
			logger.info("Remote file length : " + synchronizationMessage.getFileLength() + ", local file length : " + fileLength);
			Assert.isTrue(synchronizationMessage.getFileLength() == fileLength, "File from : " + ip
					+ " not the same, somthing went wrong in the transfer : ");
		} catch (Exception e) {
			logger.error("Exception writing index file : " + file + ", " + synchronizationMessage, e);
			written = Boolean.FALSE;
		} finally {
			try {
				getClusterManager().setWorking(IConstants.SYNCHRONIZATION, IConstants.SYNCHRONIZATION, Boolean.FALSE);
			} catch (Exception e) {
				logger.error("Cluster synchroinzation exception?", e);
			}
			try {
				if (directoryLock != null) {
					if (directoryLock.isLocked()) {
						directoryLock.release();
					}
				}
				if (directory != null) {
					directory.close();
				}
			} catch (Exception e) {
				logger.error("Exception releasing the lock on directory : " + (file != null ? file.getParentFile() : file), e);
			}
			close(inputStream);
			close(fileOutputStream);
			close(socket);
			if (!written) {
				// Delete the file as something went wrong
				logger.warn("Exception getting file : " + file + ", from host.");
				if (file != null && file.exists()) {
					FileUtilities.deleteFile(file, 1);
				}
			}
		}
	}

	protected void close(OutputStream outputStream) {
		if (outputStream != null) {
			try {
				outputStream.flush();
				outputStream.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	protected void close(InputStream inputStream) {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	protected void close(Socket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	protected void close(ServerSocket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

}