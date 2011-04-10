package ikube.action.rule;

import ikube.index.IndexManager;
import ikube.model.IndexContext;

/**
 * This class checks to see if the current index is backed up.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class IsIndexBackedUp extends ARule<IndexContext> {

	public boolean evaluate(final IndexContext indexContext) {
		String indexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);
		return indexesExist(indexDirectoryPathBackup);
	}
}