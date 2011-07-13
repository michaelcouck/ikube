package ikube.action.rule;

import ikube.model.IndexContext;

/**
 * TODO This class can be deleted.
 * 
 * This class checks to see if the current index is backed up.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class MustBackUpIndex extends ARule<IndexContext<?>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		// Check 
		logger.warn("This class needs to be re-implemented!");
		return Boolean.FALSE;
		// return indexContext.getIndexDirectoryPathBackup() != null;
	}
}