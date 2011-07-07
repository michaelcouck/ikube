package ikube.action.rule;

import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

/**
 * This rule checks to see if there are any other servers working on this index. This will allow all the servers in the cluster to start the
 * indexing of other indexes separately, i.e. to index the different indexes in parallel.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class AnyServersWorkingThisIndex implements IRule<IndexContext<?>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		return ApplicationContextManager.getBean(IClusterManager.class).anyWorking(indexContext.getIndexName());
	}

}
