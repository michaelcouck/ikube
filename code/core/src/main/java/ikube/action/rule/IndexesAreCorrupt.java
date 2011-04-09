package ikube.action.rule;

import ikube.model.IndexContext;

/**
 * TODO Implement me. This action checks to see if the indexes are still ok.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class IndexesAreCorrupt implements IRule<IndexContext> {

	@Override
	public boolean evaluate(final IndexContext indexContext) {
		return Boolean.FALSE;
	}

}
