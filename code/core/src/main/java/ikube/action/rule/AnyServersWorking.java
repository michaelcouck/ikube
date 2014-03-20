package ikube.action.rule;

import ikube.model.IndexContext;

/**
 * This rule checks to see if there are any servers working on any index in the cluster. Typically we don't want to clean the database or
 * start deleting old indexes if there are any other servers working.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class AnyServersWorking extends ARule<IndexContext> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext indexContext) {
		return clusterManager.anyWorking();
	}

}
