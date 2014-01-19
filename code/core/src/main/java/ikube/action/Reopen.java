package ikube.action;

import ikube.model.IndexContext;

/**
 * This action will re-open the indexes in the case it is a delta index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 22.06.13
 */
public class Reopen extends Action<IndexContext<?>, Boolean> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean internalExecute(final IndexContext<?> indexContext) {
        try {
            logger.info("Re-opening index searcher : " + indexContext.getName());
            return new Open().execute(indexContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}