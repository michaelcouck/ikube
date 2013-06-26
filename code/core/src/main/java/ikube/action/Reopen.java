package ikube.action;

import ikube.model.IndexContext;

/**
 * This action will re-open the indexes in the case it is a delta index.
 * 
 * @author Michael Couck
 * @since 22.06.13
 * @version 01.00
 */
public class Reopen extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean internalExecute(final IndexContext<?> indexContext) throws Exception {
		if (indexContext.isDelta()) {
			logger.info("Reopening index : " + indexContext);
			return new Open().execute(indexContext);
		}
		return Boolean.TRUE;
	}

}