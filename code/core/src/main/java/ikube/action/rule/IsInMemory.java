package ikube.action.rule;

import ikube.model.IndexContext;

/**
 * Checks to see if the index is in memory.
 * 
 * @author Michael Couck
 * @since 27.02.2011
 * @version 01.00
 */
public class IsInMemory implements IRule<IndexContext<?>> {

	/**
	 * Checks to see if the index is in memory.
	 * 
	 * @param indexContext
	 *            the index context to check if the index is in memory
	 * @return whether the index should be in memory
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		return indexContext.getInMemory();
	}

}
