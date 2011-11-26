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
public class Restore extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean execute(final IndexContext<?> indexContext) {
		long actionId = 0;
		try {
			actionId = start(indexContext, "");
			// Get the latest backup index
			String indexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);
			File latestIndexDirectoryBackup = FileUtilities.getLatestIndexDirectory(indexDirectoryPathBackup);
			String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
			// Change the name to the max age + 24 hours which will give enough time to run another index
			long time = System.currentTimeMillis() + (1000 * 60 * 60 * 24);
			String restoredIndexDirectoryPath = indexDirectoryPath + IConstants.SEP + time;
			File restoredIndexDirectory = FileUtilities.getFile(restoredIndexDirectoryPath, Boolean.TRUE);
			logger.info("Restoring index from : " + latestIndexDirectoryBackup + ", to : " + restoredIndexDirectory);
			// Copy the backup to the index directory
			FileUtilities.copyFiles(latestIndexDirectoryBackup, restoredIndexDirectory);
		} finally {
			stop(indexContext, actionId);
		}
		return Boolean.TRUE;
	}

}