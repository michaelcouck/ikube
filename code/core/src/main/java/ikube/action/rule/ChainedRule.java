package ikube.action.rule;

import ikube.model.IndexContext;

/**
 * This rule returns true always. This is intended for actions that are chained and the dependent actions needs the preceeding action to
 * have it's rule evaluate to true before it can execute it's logic.
 * 
 * @author Michael Couck
 * @since 14.01.2012
 * @version 01.00
 */
public class ChainedRule extends ARule<IndexContext<?>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		return Boolean.TRUE;
	}

}
