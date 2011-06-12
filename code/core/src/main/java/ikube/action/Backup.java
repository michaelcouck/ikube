package ikube.action;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * This class backs up the index to a place on the network in the case where the index becomes corrupted.
 * 
 * @author Michael Couck
 * @since 08.04.11
 * @version 01.00
 */
public class Backup extends Action<IndexContext, Boolean> {

	@Override
	public Boolean execute(final IndexContext indexContext) {
		try {
			getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getName(), Boolean.TRUE);
			File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath() + IConstants.SEP
					+ indexContext.getIndexName());
			String indexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);
			File latestIndexDirectoryBackup = FileUtilities.getFile(indexDirectoryPathBackup, Boolean.TRUE);
			try {
				logger.info("Backing up index from : " + latestIndexDirectory + ", to : " + latestIndexDirectoryBackup);
				// Copy the index to the designated place on the network
				FileUtils.copyDirectoryToDirectory(latestIndexDirectory, latestIndexDirectoryBackup);
				// FileUtilities.copyFiles(latestIndexDirectory, latestIndexDirectoryBackup);
			} catch (IOException e) {
				logger.error("Exception backing up indexes : ", e);
				return Boolean.FALSE;
			}
		} finally {
			getClusterManager().setWorking(indexContext.getIndexName(), "", Boolean.FALSE);
		}
		return Boolean.TRUE;
	}

}