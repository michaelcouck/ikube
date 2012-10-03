package ikube.action.rule;

import ikube.index.IndexManager;
import ikube.model.IndexContext;

import java.io.File;

/**
 * Checks to see if there is a backup index for the current.
 * 
 * @author Michael Couck
 * @since 12.07.11
 * @version 01.00
 */
public class DoesBackupIndexExist extends ARule<IndexContext<?>> {

	/**
	 * @param indexContext the index context to check if the index is expired
	 * @return whether the backup index for this index exists
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		String indexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);
		File latestIndexDirectoryBackup = IndexManager.getLatestIndexDirectory(indexDirectoryPathBackup);
		return latestIndexDirectoryBackup != null && latestIndexDirectoryBackup.exists();
	}

}
