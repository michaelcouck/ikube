package ikube.action;

import ikube.model.IndexContext;

/**
 * This class takes the searcher and tries to close the searcher on the directory.
 * 
 * @author Michael Couck
 * @since 24.08.08
 * @version 01.00
 */
public class Close extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean execute(final IndexContext<?> indexContext) {
		long actionId = 0;
		try {
			actionId = start(indexContext, "");
			closeSearchables(indexContext);
			// Set the searcher to null so the open action
			// will then be invoked to re-open the searcher
			// during the next iteration over the actions
			return Boolean.TRUE;
		} finally {
			stop(actionId);
		}
	}

}