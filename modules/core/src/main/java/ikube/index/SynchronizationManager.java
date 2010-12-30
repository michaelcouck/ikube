package ikube.index;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.listener.ListenerManager;
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

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ILock;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;

/**
 * @author Michael Couck
 * @since 19.12.10
 * @version 01.00
 */
public class SynchronizationManager implements MessageListener<SynchronizationMessage>, IListener {

	private static String READ_LOCK = "readLock";

	private Logger logger;
	/** The cluster manager to get the read lock from. */
	private IClusterManager clusterManager;
	/** The size of the chunks of data. */
	private int chunk = 1024 * 1000;
	/** The file currently being sent. */
	private File currentFile;
	/** The index of the file in the set of index files. */
	private int index = 0;
	/** The port that the server socket was opened on. */
	private int port;

	public void initialize() {
		logger = Logger.getLogger(SynchronizationManager.class);
		// We need to listen to the topic for messages that other servers
		// have published some index files
		ITopic<SynchronizationMessage> topic = Hazelcast.getTopic(IConstants.SYNCHRONIZATION_TOPIC);
		topic.addMessageListener(this);
		// We also have to listen to the scheduler so we can publish our own files to the cluster
		ListenerManager.addListener(this);
		new Thread(new Runnable() {
			public void run() {
				// Open a socket on the a synchronization port. We start with
				// the default port and iterate through the ports until we find one that
				// is available
				ServerSocket serverSocket = null;
				port = IConstants.SYNCHRONIZATION_PORT;
				while (true) {
					try {
						logger.info("Trying port : " + port);
						serverSocket = new ServerSocket(port);
						logger.info("Opened synchronization socket : " + serverSocket);
						break;
					} catch (Exception e) {
						logger.error("Exception opening a server socket : " + IConstants.SYNCHRONIZATION_PORT, e);
						port++;
						if (port >= Short.MAX_VALUE) {
							logger.warn("Couldn't find a port available for synchronization : " + port);
							return;
						}
					}
				}
				while (true) {
					try {
						// Wait for takers to access the current file
						logger.info("Waiting for clients : ");
						final Socket socket = serverSocket.accept();
						logger.info("Got client : " + socket + ", " + socket.getRemoteSocketAddress());
						new Thread(new Runnable() {
							public void run() {
								// Write the index file to the output stream
								writeFile(socket);
							}
						}).start();
					} catch (Exception e) {
						logger.error("", e);
					}
				}
			}
		}).start();
	}

	@Override
	public void handleNotification(Event event) {
		if (!event.getType().equals(Event.SYNCHRONISE)) {
			return;
		}
		// Check to see if there are any new indexes that are finished,
		// i.e. not locked
		List<File> files = getIndexFiles();
		// Publish a message to the cluster with the name
		// of the file that we want to send to each
		if (files.size() == 0) {
			logger.info("No files to distribute yet : " + files);
			return;
		}
		try {
			// This is the file we will publish
			if (index >= files.size()) {
				index = 0;
			}
			currentFile = files.get(index);
			logger.info("Publishing file : " + currentFile);
			SynchronizationMessage synchronizationMessage = new SynchronizationMessage();
			synchronizationMessage.setIp(InetAddress.getLocalHost().getHostAddress());
			synchronizationMessage.setPort(port);
			synchronizationMessage.setFilePath(currentFile.getAbsolutePath());
			synchronizationMessage.setFileLength(currentFile.length());
			logger.info("Publishing message : " + synchronizationMessage);
			Hazelcast.getTopic(IConstants.SYNCHRONIZATION_TOPIC).publish(synchronizationMessage);
			index++;
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	protected void writeFile(Socket socket) {
		FileInputStream fileInputStream = null;
		OutputStream outputStream = null;
		try {
			boolean fileExists = currentFile != null && currentFile.exists();
			logger.info("File exists : " + fileExists + ", " + currentFile);
			if (fileExists) {
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
			close(socket);
			close(fileInputStream);
		}
	}

	@Override
	public void onMessage(SynchronizationMessage synchronizationMessage) {
		ILock lock = null;
		Directory directory = null;
		Lock directoryLock = null;
		Socket socket = null;
		InputStream inputStream = null;
		FileOutputStream fileOutputStream = null;
		File file = null;
		try {
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
			File baseIndexDirectory = new File(indexContext.getIndexDirectoryPath());
			String[] patterns = new String[] { fileName };
			List<File> foundFiles = FileUtilities.findFilesRecursively(baseIndexDirectory, patterns, new ArrayList<File>());
			logger.info("Found files : " + foundFiles);
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
				logger.info("Got this file : " + filePath);
				return;
			}

			// Get the lock for a read from the cluster
			lock = getClusterManager().lock(READ_LOCK);
			if (lock == null) {
				logger.info("Couldn't get the read lock : " + lock);
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
			// Delete the files as something went wrong
			if (file != null && file.exists()) {
				FileUtilities.deleteFile(file, 1);
			}
		} finally {
			if (lock != null) {
				getClusterManager().unlock(lock);
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
			close(socket);
			close(fileOutputStream);
		}
	}

	protected List<File> getIndexFiles() {
		List<File> files = new ArrayList<File>();
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (String indexContextName : indexContexts.keySet()) {
			IndexContext indexContext = indexContexts.get(indexContextName);
			try {
				String indexDirectoryPath = indexContext.getIndexDirectoryPath();
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

	protected void close(OutputStream outputStream) {
		if (outputStream != null) {
			try {
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

	public IClusterManager getClusterManager() {
		return clusterManager;
	}

	public void setClusterManager(IClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

}