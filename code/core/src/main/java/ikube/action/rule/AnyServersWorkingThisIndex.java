package ikube.action.rule;

import ikube.model.IndexContext;

/**
 * This rule checks to see if there are any other servers working on this index. This will allow all the servers in the cluster to start the
 * indexing of other indexes separately, i.e. to index the different indexes in parallel.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class AnyServersWorkingThisIndex extends ARule<IndexContext> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext indexContext) {
		return clusterManager.anyWorking(indexContext.getIndexName());
	}

}
