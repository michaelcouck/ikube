package ikube.action;

import ikube.action.index.IndexManager;
import ikube.action.remote.SynchronizeCallable;
import ikube.action.remote.SynchronizeLatestIndexCallable;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.toolkit.FileUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * This class will synchronize a remote index with a local one. In the case each server is
 * configured to us it's own file system for the indexes, i.e. they are not shared on the network,
 * to avoid each server re-indexing the data, this class will essentially copy the remote index to
 * the local machine.
 * <p/>
 * First the remote server with the latest index is discovered. Then each file from the remote
 * index is copied a chunk at a time, using a remote callable, until the files is completely copied
 * and the chunk(binary array) is empty.
 * <p/>
 * The possible implementations are:
 * 1) Use ftp for linux
 * 2) Use the java ssh library
 * 3) Use a remote task in Hazelcast that compresses a chunk of file at a time
 * <p/>
 *
 * @author Michael Couck
 * @version 01.00
 * @since 08-04-2014
 */
@SuppressWarnings("UnusedDeclaration")
public class Synchronize extends Action<IndexContext, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean internalExecute(final IndexContext indexContext) {
		// Get the latest index on one of the remote servers
		Server remote = getTargetRemoteServer(indexContext);
		logger.debug("Remote server : " + remote);
		if (remote == null) {
			return Boolean.FALSE;
		}

		// Get all the remote index files from the target server
		String[] indexFiles;
		SynchronizeLatestIndexCallable latestCallable = new SynchronizeLatestIndexCallable(indexContext);
		Future<String[]> future = clusterManager.sendTaskTo(remote, latestCallable);
		try {
			indexFiles = future.get();
			logger.debug("Index files : " + Arrays.toString(indexFiles));
		} catch (final InterruptedException | ExecutionException e) {
			throw new RuntimeException("Exception getting the index files from the remote server : " + remote, e);
		}

		// Iterate over the index files and copy them to the local file system
		boolean exception = Boolean.FALSE;
		String currentIndexFile = null;
		try {
			// Ten megs at a time should be fine
			int length = 1024 * 1024 * 10;
			for (final String indexFile : indexFiles) {
				int offset = 0;
				logger.debug("Index file : " + indexFile);
				currentIndexFile = indexFile;
				do {
					// Keep calling the remote server for chunks of the index file
					// until there is no more data left, i.e. the files is completely copied
					byte[] chunk = getChunk(remote, indexFile, offset, length);
					if (chunk == null || chunk.length == 0) {
						break;
					}
					writeFile(indexContext, indexFile, chunk, offset);
					offset += chunk.length;
				} while (true);
			}
		} catch (final Exception e) {
			exception = Boolean.TRUE;
			throw new RuntimeException("Exception synchronizing remote index : ", e);
		} finally {
			if (exception) {
				// Try to delete the potentially corrupt index files
				//noinspection ConstantConditions
				if (currentIndexFile != null) {
					File indexDirectory = getOutputFile(indexContext, currentIndexFile);
					logger.warn("Deleting index file coming from remote server : " + indexDirectory);
					FileUtilities.deleteFile(indexDirectory);
				}
			}
		}
		return Boolean.TRUE;
	}

	byte[] getChunk(final Server remote, final String indexFile, final long offset, final long length) {
		int retry = 3;
		byte[] chunk = null;
		do {
			try {
				logger.debug("Getting file : " + indexFile + ", " + offset + ", " + length);
				SynchronizeCallable chunkCallable = new SynchronizeCallable(indexFile, offset, length);
				Future<byte[]> chunkFuture = clusterManager.sendTaskTo(remote, chunkCallable);
				chunk = chunkFuture.get();
				break;
			} catch (final Exception e) {
				logger.error("Exception getting chunk, will retry : " + retry, e);
			}
			// We'll retry a few times for this chunk
		} while (retry-- > 0);
		return chunk;
	}

	void writeFile(final IndexContext indexContext, final String indexFile, final byte[] chunk, final long offset) throws IOException {
		File file = getOutputFile(indexContext, indexFile);
		RandomAccessFile randomAccessFile = null;
		try {
			logger.debug("Offset : " + offset + ", length : " + chunk.length);
			randomAccessFile = new RandomAccessFile(file, "rw");
			randomAccessFile.seek(offset);
			randomAccessFile.write(chunk);
		} finally {
			IOUtils.closeQuietly(randomAccessFile);
		}
	}

	private File getOutputFile(final IndexContext indexContext, final String indexFile) {
		// /mnt/sdb/indexes/autocomplete/1397377466355/192.168.1.8-8022
		String[] segments = StringUtils.split(indexFile, File.separator);
		String timestampDirectory = segments[segments.length - 3];
		String serverDirectory = segments[segments.length - 2];
		String fileName = segments[segments.length - 1];

		String indexDirectory = IndexManager.getIndexDirectoryPath(indexContext);

		StringBuilder builder = new StringBuilder(indexDirectory);
		builder.append(File.separator);
		builder.append(timestampDirectory);
		builder.append(File.separator);
		builder.append(serverDirectory);
		builder.append(File.separator);
		builder.append(fileName);

		logger.debug("Writing to file : " + builder.toString());

		return FileUtilities.getOrCreateFile(new File(builder.toString()));
	}

	Server getTargetRemoteServer(final IndexContext indexContext) {
		Server remote = null;
		Date timestamp = null;
		Server local = clusterManager.getServer();
		Map<String, Server> servers = clusterManager.getServers();
		for (final Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			if (server.getAddress().equals(local.getAddress())) {
				continue;
			}
			List<IndexContext> indexContexts = server.getIndexContexts();
			Date latestIndexTimestamp = getLatestIndexTimestamp(indexContext, indexContexts);
			if (timestamp == null) {
				timestamp = latestIndexTimestamp;
				remote = server;
			} else {
				if (latestIndexTimestamp != null && latestIndexTimestamp.after(timestamp)) {
					timestamp = latestIndexTimestamp;
					remote = server;
				}
			}
		}
		return remote;
	}

	Date getLatestIndexTimestamp(final IndexContext indexContext, final List<IndexContext> indexContexts) {
		for (final IndexContext remoteIndexContext : indexContexts) {
			if (remoteIndexContext.getName().equals(indexContext.getName())) {
				Snapshot snapshot = remoteIndexContext.getSnapshot();
				if (snapshot != null) {
					return snapshot.getLatestIndexTimestamp();
				}
			}
		}
		return null;
	}

}