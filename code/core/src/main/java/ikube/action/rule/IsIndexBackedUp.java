package ikube.action.rule;

import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;

/**
 * This class checks to see if the current index is backed up.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class IsIndexBackedUp extends ARule<IndexContext<?>> {

	public boolean evaluate(final IndexContext<?> indexContext) {
		// See if there is a latest index directory that is finished and not corrupt
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
		if (latestIndexDirectory == null) {
			return Boolean.FALSE;
		}
		boolean indexExists = indexesExist(IndexManager.getIndexDirectoryPath(indexContext));
		if (!indexExists) {
			return Boolean.FALSE;
		}
		// There is a new index, lets see if it has been backed up
		File latestIndexDirectoryBackup = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPathBackup());
		if (latestIndexDirectoryBackup == null) {
			return Boolean.FALSE;
		}
		// Lets see if the backup index is the same timestamp as the latest index directory
		boolean areDirectoriesEqual = new AreDirectoriesEqual().evaluate(new File[] { latestIndexDirectory, latestIndexDirectoryBackup });
		if (!areDirectoriesEqual) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
}