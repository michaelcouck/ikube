package ikube.action.rule;

import ikube.model.IndexContext;

import org.apache.log4j.Logger;
import org.apache.lucene.search.MultiSearcher;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class IsMultiSearcherInitialised implements IRule<IndexContext> {

	private Logger logger = Logger.getLogger(this.getClass());

	public boolean evaluate(IndexContext indexContext) {
		MultiSearcher multiSearcher = indexContext.getIndex().getMultiSearcher();
		if (multiSearcher == null) {
			logger.debug("Multi searcher null, should try to reopen : ");
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

}
