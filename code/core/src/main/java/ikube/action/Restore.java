package ikube.action;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * This class restores an index that has been back up to the index directory designated for indexes.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 08-04-2011
 */
public class Restore extends Action<IndexContext, Boolean> {

    /**
     * {@inheritDoc}
     */
    @Override
    boolean internalExecute(final IndexContext indexContext) {
        // Check that the index directory and the backup directory are not the same
        if (indexContext.getIndexDirectoryPath().equals(indexContext.getIndexDirectoryPathBackup())) {
            logger.debug("Index and backup paths are the same : ");
            return Boolean.FALSE;
        }
        File latestIndexDirectoryBackup;
        File restoredIndexDirectory;
        // Get the latest backup index
        String indexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);
        latestIndexDirectoryBackup = IndexManager.getLatestIndexDirectory(indexDirectoryPathBackup);
        String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
        // Change the name to the max age + 24 hours which will give enough time to run another index
        long time = System.currentTimeMillis() + (1000 * 60 * 60 * 24);
        String restoredIndexDirectoryPath = indexDirectoryPath + IConstants.SEP + time;
        restoredIndexDirectory = new File(restoredIndexDirectoryPath);
        logger.info("Restoring index from : " + latestIndexDirectoryBackup + ", to : " + restoredIndexDirectory);
        try {
            // Move the backup to the hot index, tne backup action will back it up again if necessary
            FileUtils.moveDirectory(latestIndexDirectoryBackup, restoredIndexDirectory);
            // Try to back it up immediately any how
            FileUtils.copyDirectory(restoredIndexDirectory, latestIndexDirectoryBackup);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Boolean.TRUE;
    }

}