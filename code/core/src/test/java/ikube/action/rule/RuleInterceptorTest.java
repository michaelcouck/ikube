package ikube.action.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.action.Close;
import ikube.action.IAction;
import ikube.action.rule.AreIndexesCreated;
import ikube.action.rule.AreSearchablesInitialised;
import ikube.action.rule.AreUnopenedIndexes;
import ikube.action.rule.IRule;
import ikube.action.rule.IsIndexCurrent;
import ikube.action.rule.IsMultiSearcherInitialised;
import ikube.action.rule.RuleInterceptor;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.model.IndexContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

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

	public RuleInterceptorTest() {
		super(RuleInterceptorTest.class);
	}

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
		String message = "Expected : " + expected + " result : " + result;
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
	public void jep() {
		JEP jep = new JEP();
		jep.addVariable("a", Boolean.FALSE);
		jep.addVariable("b", Boolean.FALSE);
		jep.addVariable("c", Boolean.TRUE);
		jep.addVariable("d", Boolean.TRUE);
		jep.addVariable("e", Boolean.TRUE);
		jep.parseExpression("((a || b) || !c) && !(d && e)");
		Object result = jep.getValueAsObject();
		logger.info("Jep result : " + result);
		assertNotNull(result);
	}

}
