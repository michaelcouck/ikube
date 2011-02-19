package ikube.action.rule;

import ikube.model.IndexContext;

import org.apache.log4j.Logger;
import org.apache.lucene.search.Searchable;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class AreSearchablesInitialised implements IRule<IndexContext> {

	private Logger logger = Logger.getLogger(this.getClass());

	private boolean expected;

	public boolean evaluate(IndexContext indexContext) {
		// No searchables, also try to reopen an index searcher
		Searchable[] searchables = indexContext.getIndex().getMultiSearcher().getSearchables();
		if (searchables == null || searchables.length == 0) {
			logger.debug("No searchables open, should try to reopen : ");
			return Boolean.FALSE == expected;
		}
		return Boolean.TRUE == expected;
	}

	@Override
	public void setExpected(boolean expected) {
		this.expected = expected;
	}

}
