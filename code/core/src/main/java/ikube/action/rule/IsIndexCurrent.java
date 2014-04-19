package ikube.action.rule;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;

/**
 * Checks to see if the index for the index context is current.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-02-2011
 */
public class IsIndexCurrent extends ARule<IndexContext> {

    /**
     * Checks to see if the current index is not passed it's expiration period. Each index had a
     * parent directory that is a long of the system time that the index was started. This time signifies
     * the age of the index.
     *
     * @param indexContext the index context to check if the index is expired
     * @return whether the index for this index context is passed it's expiration date
     */
    @Override
    public boolean evaluate(final IndexContext indexContext) {
        String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		boolean indexCurrent = isIndexCurrent(indexContext, indexDirectoryPath);
		logger.info("Is index current : " + indexCurrent + ", " + indexContext + ", " + indexDirectoryPath);
        return indexCurrent;
    }

}
