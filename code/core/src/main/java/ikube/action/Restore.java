package ikube.action;

import ikube.IConstants;
import ikube.index.IndexManager;
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
	boolean executeInternal(final IndexContext<?> indexContext) {
		ikube.model.Action action = null;
		File latestIndexDirectoryBackup = null;
		File restoredIndexDirectory = null;
		try {
			action = start(indexContext.getIndexName(), "");
			// Get the latest backup index
			String indexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);
			latestIndexDirectoryBackup = IndexManager.getLatestIndexDirectory(indexDirectoryPathBackup);
			String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
			// Change the name to the max age + 24 hours which will give enough time to run another index
			long time = System.currentTimeMillis() + (1000 * 60 * 60 * 24);
			String restoredIndexDirectoryPath = indexDirectoryPath + IConstants.SEP + time;
			restoredIndexDirectory = FileUtilities.getFile(restoredIndexDirectoryPath, Boolean.TRUE);
			logger.info("Restoring index from : " + latestIndexDirectoryBackup + ", to : " + restoredIndexDirectory);
			// Copy the backup to the index directory
			// FileUtilities.copyFiles(latestIndexDirectoryBackup, restoredIndexDirectory);
			FileUtils.copyDirectory(latestIndexDirectoryBackup, restoredIndexDirectory);
		} catch (IOException e) {
			logger.error("Exception restoring index from backup : backup : " + latestIndexDirectoryBackup + ", restored : "
					+ restoredIndexDirectory, e);
		} finally {
			stop(action);
		}
		return Boolean.TRUE;
	}

}