package ikube.action.rule;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;

import java.io.File;

/**
 * This class checks to see if the current index is backed up.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class IsIndexBackedUp extends ARule<IndexContext> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext indexContext) {
		// See if there is a latest index directory that is finished and not corrupt
		String latestIndexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(latestIndexDirectoryPath);
		logger.debug("Latest index directory : " + latestIndexDirectory);
		if (latestIndexDirectory == null) {
			return Boolean.FALSE;
		}
		boolean indexExists = indexesExist(IndexManager.getIndexDirectoryPath(indexContext));
		logger.debug("Latest index directory exists : " + indexExists);
		if (!indexExists) {
			return Boolean.FALSE;
		}
		// There is a new index, lets see if it has been backed up
		String indexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);
		File latestIndexDirectoryBackup = IndexManager.getLatestIndexDirectory(indexDirectoryPathBackup);
		boolean backupExists = latestIndexDirectoryBackup != null && latestIndexDirectoryBackup.exists()
				&& latestIndexDirectoryBackup.listFiles().length > 0;
		logger.debug("Backup index directory exists : " + backupExists);
		if (!backupExists) {
			return Boolean.FALSE;
		}
		// Lets see if the backup index is the same timestamp as the latest index directory
		boolean areDirectoriesEqual = new AreDirectoriesEqual().evaluate(new File[] { latestIndexDirectory, latestIndexDirectoryBackup });
		logger.debug("Directories equal : " + areDirectoriesEqual + ", " + latestIndexDirectory + ", " + latestIndexDirectoryBackup);
		if (!areDirectoriesEqual) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
}