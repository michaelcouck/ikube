package ikube.action.rule;

import ikube.ATest;
import ikube.action.IAction;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test can be used ad-hoc to see if the rules are configured property.
 * 
 * @author Michael Couck
 * @since 20.03.11
 * @version 01.00
 */
@Ignore
public class RulesTest extends ATest {

	private Logger logger = Logger.getLogger(this.getClass());

	public RulesTest() {
		super(RulesTest.class);
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void evaluate() {
		Map<String, IAction> actions = ApplicationContextManager.getBeans(IAction.class);
		for (IAction action : actions.values()) {
			List<IRule<IndexContext>> rules = action.getRules();
			for (IRule<IndexContext> rule : rules) {
				boolean result = rule.evaluate(indexContext);
				logger.info("Rule : " + rule + ", result : " + result);
			}
		}
	}

}
