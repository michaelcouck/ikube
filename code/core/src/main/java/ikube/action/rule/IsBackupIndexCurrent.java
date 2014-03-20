package ikube.action.rule;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;

/**
 * Checks to see if the backup index for the index context is current.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class IsBackupIndexCurrent extends ARule<IndexContext> {

	/**
	 * Checks to see whether the backup index is still current.
	 * 
	 * @param indexContext
	 *            the index context to check if the index is expired
	 * @return whether the backup index for this index context is passed it's expiration date
	 */
	@Override
	public boolean evaluate(final IndexContext indexContext) {
		String indexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);
		return isIndexCurrent(indexContext, indexDirectoryPathBackup);
	}

}
