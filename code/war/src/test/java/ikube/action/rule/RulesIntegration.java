package ikube.action.rule;

import ikube.IntegrationTest;
import ikube.action.IAction;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * This test can be used ad-hoc to see if the rules are configured property.
 * 
 * @author Michael Couck
 * @since 20.03.11
 * @version 01.00
 */
public class RulesIntegration extends IntegrationTest {

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void evaluate() {
		Map<String, IAction> actions = ApplicationContextManager.getBeans(IAction.class);
		for (IAction action : actions.values()) {
			Object rules = action.getRules();
			if (List.class.isAssignableFrom(rules.getClass())) {
				for (IRule<IndexContext> rule : (List<IRule<IndexContext>>) rules) {
					for (final IndexContext indexContext : ApplicationContextManager.getBeans(IndexContext.class).values()) {
						rule.evaluate(indexContext);
					}
				}
			}
		}
	}

}
