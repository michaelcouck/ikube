package ikube.action.rule;

import ikube.model.IndexContext;
import org.apache.log4j.Logger;

/**
 * This rule checks to see if the index searcher is opened on an index. If it is
 * not open then the open action should proceed to try open the searcher.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-02-2011
 */
public class IsMultiSearcherInitialised implements IRule<IndexContext> {

    private static final transient Logger LOGGER = Logger.getLogger(IsMultiSearcherInitialised.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean evaluate(final IndexContext indexContext) {
        if (indexContext.getMultiSearcher() == null) {
            LOGGER.info("Multi searcher null, should try to reopen : " + indexContext.getName());
            return Boolean.FALSE;
        }
        if (indexContext.getMultiSearcher().getIndexReader().numDocs() == 0) {
            LOGGER.info("No documents in index : " + indexContext.getName() + ", returning false for open index : ");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}
