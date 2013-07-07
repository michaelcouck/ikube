package ikube.action.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.action.Action;
import ikube.action.Close;
import ikube.action.IAction;
import ikube.action.Index;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.model.IndexContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nfunk.jep.JEP;

/**
 * @author Michael Couck
 * @since 26.02.2011
 * @version 01.00
 */
public class RuleInterceptorTest extends AbstractTest {

	@MockClass(realClass = JEP.class)
	public static class JEPMock {
		@Mock()
		public Object getValueAsObject() {
			return new Double("1.0");
		}
	}

	@SuppressWarnings("rawtypes")
	private Map<String, IAction> actions;

	private ProceedingJoinPoint joinPoint;
	/** Class under test. */
	private RuleInterceptor ruleInterceptor;

	private IAction<IndexContext<?>, ?> action;
	private IsMultiSearcherInitialised isMultiSearcherInitialised;
	private AreSearchablesInitialised areSearchablesInitialised;
	private IsIndexCurrent isIndexCurrent;
	private AreIndexesCreated areIndexesCreated;
	private AreUnopenedIndexes areUnopenedIndexes;

	@Cascading
	IsIndexCurrent isIndexCurrentBug;
	@Cascading
	AnyServersWorkingThisIndex anyServersWorkingThisIndexBug;
	@Cascading
	TooManyActionsRule tooManyActionsRuleBug;
	@Cascading
	IsThisServerWorking isThisServerWorkingBug;
	@Cascading
	AreOtherServers areOtherServersBug;
	@Cascading
	AnyServersIdle anyServersIdleBug;

	@Before
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void before() throws Throwable {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class, JEPMock.class);
		actions = new HashMap<String, IAction>();

		final List<IRule<IndexContext<?>>> rules = new ArrayList<IRule<IndexContext<?>>>();

		isMultiSearcherInitialised = mock(IsMultiSearcherInitialised.class);
		areSearchablesInitialised = mock(AreSearchablesInitialised.class);
		isIndexCurrent = mock(IsIndexCurrent.class);
		areIndexesCreated = mock(AreIndexesCreated.class);
		areUnopenedIndexes = mock(AreUnopenedIndexes.class);

		action = mock(IAction.class);
		Close close = mock(Close.class);

		rules.add(isMultiSearcherInitialised);
		rules.add(areSearchablesInitialised);
		rules.add(isIndexCurrent);
		rules.add(areIndexesCreated);
		rules.add(areUnopenedIndexes);

		StringBuilder builder = new StringBuilder();

		boolean first = Boolean.TRUE;
		for (IRule rule : rules) {
			if (!first) {
				builder.append(" && ");
			}
			first = Boolean.FALSE;
			builder.append(rule.getClass().getSimpleName());
		}

		final String predicate = builder.toString();
		when(close.getRuleExpression()).thenReturn(predicate);
		when(close.getRules()).thenReturn(rules);
		when(close.execute(any(IndexContext.class))).thenReturn(Boolean.TRUE);

		joinPoint = mock(ProceedingJoinPoint.class);
		when(joinPoint.getTarget()).thenReturn(close);
		when(joinPoint.getArgs()).thenReturn(new Object[] { indexContext });
		when(joinPoint.proceed(any(Object[].class))).thenReturn(Boolean.TRUE);
		ruleInterceptor = new RuleInterceptor();

		when(action.getRules()).thenReturn(rules);
		when(action.getRuleExpression()).thenReturn(predicate);

		actions.put(action.toString(), action);

		Deencapsulation.setField(ruleInterceptor, clusterManager);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void decide() throws Throwable {
		when(isMultiSearcherInitialised.evaluate(indexContext)).thenReturn(Boolean.TRUE);
		when(areSearchablesInitialised.evaluate(indexContext)).thenReturn(Boolean.FALSE);
		when(isIndexCurrent.evaluate(indexContext)).thenReturn(Boolean.TRUE);
		when(areIndexesCreated.evaluate(indexContext)).thenReturn(Boolean.FALSE);
		when(areUnopenedIndexes.evaluate(indexContext)).thenReturn(Boolean.TRUE);

		Object result = ruleInterceptor.decide(joinPoint);
		Object expected = true;
		String message = "Expected : " + expected + " category : " + result;
		logger.info("Result : " + message);
		assertEquals(message, expected, result);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void decideActions() throws Throwable {
		for (IAction action : actions.values()) {
			when(joinPoint.getTarget()).thenReturn(action);
			Object result = ruleInterceptor.decide(joinPoint);
			logger.debug("Result : " + result);
		}
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void decidePredicate() {
		new Expectations() {
			{
				anyServersWorkingThisIndexBug.evaluate(indexContext);
				result = true;
				isThisServerWorkingBug.evaluate(indexContext);
				result = true;
			}
		};

		Action action = new Index();
		action.setRuleExpression("!IsIndexCurrent && !AnyServersWorkingThisIndex && !TooManyActionsRule && !(IsThisServerWorking && AreOtherServers && AnyServersIdle)");
		action.setRequiresClusterLock(false);
		action.setRules(Arrays.asList(isIndexCurrentBug, anyServersWorkingThisIndexBug, tooManyActionsRuleBug, isThisServerWorkingBug, areOtherServersBug,
				anyServersIdleBug));
		boolean result = ruleInterceptor.evaluateRules(indexContext, action);
		logger.info("Result : " + result);
		assertFalse(result);
	}

	@Test
	public void jep() {
		JexlEngine jexl = new JexlEngine();
		Expression e = jexl.createExpression("((a || b) || !c) && !(d && e)");

		// populate the context
		JexlContext context = new MapContext();
		context.set("a", true);
		context.set("b", true);
		context.set("c", true);
		context.set("d", true);
		context.set("e", true);

		// work it out
		Object result = e.evaluate(context);
		logger.info("Result : " + result);

		e = jexl.createExpression("a");
		result = e.evaluate(context);
		logger.info("Result : " + result);
	}

}
