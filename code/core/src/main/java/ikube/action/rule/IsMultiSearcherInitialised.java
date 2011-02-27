package ikube.action.rule;

import ikube.model.IndexContext;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class IsMultiSearcherInitialised implements IRule<IndexContext> {

	private Logger logger = Logger.getLogger(this.getClass());

	public boolean evaluate(IndexContext indexContext) {
		if (indexContext.getIndex() == null) {
			return Boolean.FALSE;
		}
		if (indexContext.getIndex().getMultiSearcher() == null) {
			logger.debug("Multi searcher null, should try to reopen : ");
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

}
