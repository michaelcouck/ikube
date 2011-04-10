package ikube.action;

import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;

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
			String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
			File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexDirectoryPath);
			String indexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);
			File latestIndexDirectoryBackup = FileUtilities.getFile(indexDirectoryPathBackup, Boolean.TRUE);
			// Copy the index to the designated place on the network
			FileUtilities.copyFiles(latestIndexDirectory, latestIndexDirectoryBackup);
		} finally {
			getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getName(), Boolean.FALSE);
		}
		return Boolean.TRUE;
	}

}