package ikube.action;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

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
	boolean internalExecute(final IndexContext<?> indexContext) {
		// Check that the index directory and the backup directory are not the same
		if (indexContext.getIndexDirectoryPath().equals(indexContext.getIndexDirectoryPathBackup())) {
			logger.info("Index and backup paths are the same : ");
			return Boolean.FALSE;
		}
		File latestIndexDirectoryBackup = null;
		File restoredIndexDirectory = null;
		// Get the latest backup index
		String indexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);
		latestIndexDirectoryBackup = IndexManager.getLatestIndexDirectory(indexDirectoryPathBackup);
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		// Change the name to the max age + 24 hours which will give enough time to run another index
		long time = System.currentTimeMillis() + (1000 * 60 * 60 * 24);
		String restoredIndexDirectoryPath = indexDirectoryPath + IConstants.SEP + time;
		restoredIndexDirectory = FileUtilities.getFile(restoredIndexDirectoryPath, Boolean.TRUE);
		logger.info("Restoring index from : " + latestIndexDirectoryBackup + ", to : " + restoredIndexDirectory);
		try {
			// Copy the backup to the index directory
			FileUtils.copyDirectory(latestIndexDirectoryBackup, restoredIndexDirectory);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return Boolean.TRUE;
	}

}