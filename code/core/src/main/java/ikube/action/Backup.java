package ikube.action;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.apache.commons.io.FileUtils;

/**
 * This class backs up the index to a place on the network in the case where the index becomes corrupted.
 * 
 * @author Michael Couck
 * @since 08.04.11
 * @version 01.00
 */
public class Backup extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean executeInternal(final IndexContext<?> indexContext) {
		ikube.model.Action action = null;
		try {
			action = start(indexContext.getIndexName(), "");
			String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
			File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexDirectoryPath);
			if (latestIndexDirectory == null || !latestIndexDirectory.exists()) {
				logger.warn("No index created for context : " + indexContext.getName());
				return Boolean.FALSE;
			}
			String indexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);
			File indexDirectoryBackup = FileUtilities.getFile(indexDirectoryPathBackup, Boolean.TRUE);
			if (indexDirectoryBackup == null || !indexDirectoryBackup.exists()) {
				logger.warn("Backup directory could not be created : " + indexDirectoryBackup);
			}
			try {
				String latestIndexDirectoryBackupPath = indexDirectoryPathBackup + IConstants.SEP + latestIndexDirectory.getName();
				File latestIndexDirectoryBackup = FileUtilities.getFile(latestIndexDirectoryBackupPath, Boolean.TRUE);
				logger.info("Backing up index from : " + latestIndexDirectory + ", to : " + latestIndexDirectoryBackup);
				// Copy the index to the designated place on the network
				// FileUtilities.copyFiles(latestIndexDirectory, latestIndexDirectoryBackup);
				FileUtils.copyDirectory(latestIndexDirectory, latestIndexDirectoryBackup);
				logger.info("Backed up index from : " + latestIndexDirectory + ", to : " + latestIndexDirectoryBackup);
			} catch (Exception e) {
				logger.error("Exception backing up indexes : ", e);
				return Boolean.FALSE;
			}
		} finally {
			stop(action);
		}
		return Boolean.TRUE;
	}

}