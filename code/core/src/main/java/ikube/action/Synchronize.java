package ikube.action;

import ikube.IConstants;
import ikube.action.remote.SynchronizeLatestIndexCallable;
import ikube.model.IndexContext;
import ikube.toolkit.StringUtilities;

import java.util.Date;
import java.util.List;
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
        SynchronizeLatestIndexCallable synchronizeLatestIndexCallable = new SynchronizeLatestIndexCallable(indexContext);
        List<Future<String[]>> futures = clusterManager.sendTaskToAll(synchronizeLatestIndexCallable);
        Future<String[]> latestFuture = null;
        for (final Future<String[]> future : futures) {
            if (latestFuture == null) {
                latestFuture = future;
                continue;
            }
            try {
                String[] indexFiles = future.get();
                String[] latestIndexFiles = latestFuture.get();
                if (indexFiles == null || indexFiles.length == 0) {
                    continue;
                }
                if (latestIndexFiles == null || latestIndexFiles.length == 0) {
                    latestFuture = future;
                    continue;
                }
                // Compare the index timestamps
                long indexDate = getDirectoryDate(indexFiles[0]);
                long latestIndexDate = getDirectoryDate(latestIndexFiles[0]);
                if (indexDate > latestIndexDate) {
                    latestFuture = future;
                }
            } catch (final InterruptedException | ExecutionException e) {
                logger.error("Exception checking future for index files : ", e);
            }
        }
        // We have the latest future from the remote servers

        return Boolean.TRUE;
    }

    long getDirectoryDate(final String filePath) {
        String[] segments = filePath.split(IConstants.SEP);
        for (final String segment : segments) {
            if (StringUtilities.isNumeric(segment)) {
                return Long.parseLong(segment);
            }
        }
        return Long.MIN_VALUE;
    }

}