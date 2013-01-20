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
	public boolean internalExecute(final IndexContext<?> indexContext) {
		ikube.model.Action action = null;
		try {
			action = start(indexContext.getIndexName(), "");
			closeSearchables(indexContext);
			// Set the searcher to null so the open action
			// will then be invoked to re-open the searcher
			// during the next iteration over the actions
			return Boolean.TRUE;
		} finally {
			stop(action);
		}
	}

}