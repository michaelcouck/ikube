package ikube.action.rule;

import ikube.model.IndexContext;

/**
 * This rule checks to see if there are any other servers in the cluster.
 * 
 * @author Michael Couck
 * @since 16.12.2011
 * @version 01.00
 */
public class AreOtherServers extends ARule<IndexContext> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext indexContext) {
		return clusterManager.getServers().size() > 1;
	}

}
