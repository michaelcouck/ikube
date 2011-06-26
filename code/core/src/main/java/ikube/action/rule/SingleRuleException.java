package ikube.action.rule;

import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

/**
 * This rule is for one exception to be made, regardless of the outcome of the other rules. For example if you want to force an index to be
 * built then this rule will be made to evaluate to true. Once the rule has been executed then it goes back to false. Typically this rule
 * will be included as an || in the expression.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class SingleRuleException extends ARule<IndexContext<?>> {

	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		return ApplicationContextManager.getBean(IClusterManager.class).isException();
	}

}
