package ikube.action;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;

/**
 * This class restores an index that has been back up to the index directory designated for indexes.
 * 
 * @author Michael Couck
 * @since 08.04.11
 * @version 01.00
 */
public class Restore extends Action<IndexContext, Boolean> {

	@Override
	public Boolean execute(final IndexContext indexContext) {
		try {
			getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getName(), Boolean.TRUE);
			// Get the latest backup index
			// Copy the backup to the index directory
			// Change the name to the max age + 1 hour
			File latestIndexDirectoryBackup = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPathBackup());
			String indexDirectoryPath = IndexManager.getIndexDirectoryPathBackup(indexContext);
			long time = System.currentTimeMillis() + (1000 * 60 * 60);
			String restoredIndexDirectoryPath = indexDirectoryPath + IConstants.SEP + time;
			File restoredIndexDirectory = FileUtilities.getFile(restoredIndexDirectoryPath, Boolean.TRUE);
			logger.info("Restoring index from : " + latestIndexDirectoryBackup + ", to : " + restoredIndexDirectory);
			FileUtilities.copyFiles(latestIndexDirectoryBackup, restoredIndexDirectory);
		} finally {
			getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getName(), Boolean.FALSE);
		}
		return Boolean.TRUE;
	}

}