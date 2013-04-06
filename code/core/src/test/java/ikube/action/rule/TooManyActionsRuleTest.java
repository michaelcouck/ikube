package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.model.Action;

import java.util.ArrayList;
import java.util.List;

import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 16.01.12
 * @version 01.00
 */
public class TooManyActionsRuleTest extends AbstractTest {

	private TooManyActionsRule tooManyActionsRule;

	public TooManyActionsRuleTest() {
		super(TooManyActionsRuleTest.class);
	}

	@Before
	public void before() {
		tooManyActionsRule = new TooManyActionsRule();
		Deencapsulation.setField(tooManyActionsRule, clusterManager);
		Deencapsulation.setField(tooManyActionsRule, 3);
	}

	@Test
	public void evaluate() {
		boolean result = tooManyActionsRule.evaluate(indexContext);
		assertFalse("There is only one action : ", result);
		List<Action> actions = new ArrayList<Action>(100);
		for (int i = 0; i < 100; i++) {
			actions.add(action);
		}
		when(server.getActions()).thenReturn(actions);
		result = tooManyActionsRule.evaluate(indexContext);
		assertTrue("There are too many actions : ", result);
	}

}