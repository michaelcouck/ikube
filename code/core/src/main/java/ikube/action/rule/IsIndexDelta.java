package ikube.action.rule;

import ikube.model.IndexContext;

/**
 * @author Michael Couck
 * @since 21.06.13
 * @version 01.00
 */
public class IsIndexDelta extends ARule<IndexContext<?>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		return indexContext.isDelta();
	}

}