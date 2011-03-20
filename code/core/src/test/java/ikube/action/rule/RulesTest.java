package ikube.action.rule;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import ikube.ATest;
import ikube.action.Index;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

/**
 * This test can be used ad-hoc to see if the rules are configured property.
 * 
 * @author Michael Couck
 * @since 20.03.11
 * @version 01.00
 */
public class RulesTest extends ATest {

	private Logger logger = Logger.getLogger(this.getClass());

	public RulesTest() {
		super(RulesTest.class);
	}

	@Test
	public void evaluate() {
		Index index = ApplicationContextManager.getBean(Index.class);
		List<IRule<IndexContext>> rules = index.getRules();
		for (IRule<IndexContext> rule : rules) {
			boolean result = rule.evaluate(INDEX_CONTEXT);
			logger.info("Rule : " + rule + ", result : " + result);
		}
	}

}
