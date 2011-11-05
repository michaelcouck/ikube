package ikube.action.rule;

import ikube.model.IndexContext;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class IsThisServerWorking extends ARule<IndexContext<?>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		return getClusterManager().getServer().getWorking();
	}

}
