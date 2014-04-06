package ikube.action;

import ikube.action.remote.SynchronizeCallable;
import ikube.action.remote.SynchronizeLatestIndexCallable;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.toolkit.FileUtilities;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * TODO: This must still be completed.
 * <p/>
 * The possible implementations are:
 * 1) User ftp for linux
 * 2) User the java ssh library
 * 3) User a remote task in Hazelcast that compresses a chunk of file at a time
 * <p/>
 * This action will synchronize indexes between servers.
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
        Server remote = getTargetRemoteServer(indexContext);
        if (remote == null) {
            return Boolean.FALSE;
        }

        String[] indexFiles;
        SynchronizeLatestIndexCallable latestCallable = new SynchronizeLatestIndexCallable(indexContext);
        Future<String[]> future = clusterManager.sendTaskTo(remote, latestCallable);
        try {
            indexFiles = future.get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException("Exception getting the index files from the remote server : " + remote);
        }

        boolean exception = Boolean.FALSE;
        try {
            int offset = 0;
            int length = 1024 * 1024 * 10;
            for (final String indexFile : indexFiles) {
                byte[] chunk;
                do {
                    SynchronizeCallable chunkCallable = new SynchronizeCallable(indexFile, offset, length);
                    Future<byte[]> chunkFuture = clusterManager.sendTaskTo(remote, chunkCallable);
                    chunk = chunkFuture.get();
                    if (chunk.length > 0) {
                        File file = new File(indexFile);
                        RandomAccessFile randomAccessFile = null;
                        try {
                            randomAccessFile = new RandomAccessFile(indexFile, "rw");
                            randomAccessFile.seek(offset);
                            randomAccessFile.write(chunk);
                        } finally {
                            IOUtils.closeQuietly(randomAccessFile);
                        }
                    }
                    offset += chunk.length;
                } while (chunk.length > 0);
            }
        } catch (final Exception e) {
            exception = Boolean.TRUE;
            throw new RuntimeException("Exception synchronizing remote index : ", e);
        } finally {
            if (exception) {
                // Try to delete the potentially corrupt index files
                //noinspection ConstantConditions
                if (indexFiles != null) {
                    File indexDirectory = new File(indexFiles[0]).getParentFile();
                    logger.warn("Deleting index file coming from remote server : " + indexDirectory);
                    FileUtilities.deleteFile(indexDirectory);
                }
            }
        }
        return Boolean.TRUE;
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
                if (latestIndexTimestamp.after(timestamp)) {
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