package ikube.action.rule;

import ikube.model.IndexContext;

/**
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
		return indexContext.getIndexDirectoryPathBackup() != null;
	}
}