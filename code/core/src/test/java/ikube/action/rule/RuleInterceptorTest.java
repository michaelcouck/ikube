package ikube.action.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.action.Close;
import ikube.action.IAction;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.model.IndexContext;
import ikube.toolkit.Logging;
import ikube.toolkit.PermutationUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import mockit.NonStrict;
import mockit.NonStrictExpectations;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nfunk.jep.JEP;

/**
 * @author Michael Couck
 * @since 26.02.2011
 * @version 01.00
 */
public class RuleInterceptorTest extends ATest {

	@MockClass(realClass = JEP.class)
	public static class JEPMock {

		@Mock()
		public Object getValueAsObject() {
			return new Double("1.0");
		}

	}

	// @MockClass(realClass = AtomicAction.class)
	// public static class AtomicActionMock {
	//
	// @Mock()
	// public ILock lock(String lockName) {
	// return mock(ILock.class);
	// }
	//
	// }

	@SuppressWarnings("rawtypes")
	private static Map<String, IAction>				ACTIONS;

	private transient ProceedingJoinPoint			joinPoint;
	private transient IRuleInterceptor				ruleInterceptor;

	private transient final List<Boolean[]>			matrix	= new ArrayList<Boolean[]>();
	private transient final Boolean[]				vector	= { Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE };

	@NonStrict
	private IAction<?, ?>							action;
	private transient IsMultiSearcherInitialised	isMultiSearcherInitialised;
	private transient AreSearchablesInitialised		areSearchablesInitialised;
	private transient IsIndexCurrent				isIndexCurrent;
	private transient AreIndexesCreated				areIndexesCreated;
	private transient AreUnopenedIndexes			areUnopenedIndexes;

	public RuleInterceptorTest() {
		super(RuleInterceptorTest.class);
	}

	@BeforeClass
	@SuppressWarnings("rawtypes")
	public static void beforeClass() {
		ACTIONS = new HashMap<String, IAction>();
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class/* , AtomicActionMock.class */);
	}

	@AfterClass
	public static void afterClass() {
		Mockit.tearDownMocks();
	}

	@Before
	public void before() throws Throwable {
		new PermutationUtilities().getPermutations(vector, matrix, vector.length, 0);
		final List<IRule<IndexContext<?>>> rules = new ArrayList<IRule<IndexContext<?>>>();

		isMultiSearcherInitialised = mock(IsMultiSearcherInitialised.class);
		areSearchablesInitialised = mock(AreSearchablesInitialised.class);
		isIndexCurrent = mock(IsIndexCurrent.class);
		areIndexesCreated = mock(AreIndexesCreated.class);
		areUnopenedIndexes = mock(AreUnopenedIndexes.class);

		rules.add(isMultiSearcherInitialised);
		rules.add(areSearchablesInitialised);
		rules.add(isIndexCurrent);
		rules.add(areIndexesCreated);
		rules.add(areUnopenedIndexes);

		Close close = mock(Close.class);
		StringBuilder builder = new StringBuilder();

		boolean first = Boolean.TRUE;
		for (@SuppressWarnings("rawtypes")
		IRule rule : rules) {
			if (!first) {
				builder.append(" && ");
			}
			first = Boolean.FALSE;
			builder.append(rule.getClass().getSimpleName());
		}

		// IsMultiSearcherInitialised && AreSearchablesInitialised && !IsIndexCurrent && AreIndexesCreated &&
		// AreUnopenedIndexes
		final String predicate = builder.toString();
		when(close.getRuleExpression()).thenReturn(predicate);
		when(close.getRules()).thenReturn(rules);
		when(close.execute(any(IndexContext.class))).thenReturn(Boolean.TRUE);

		joinPoint = mock(ProceedingJoinPoint.class);
		when(joinPoint.getTarget()).thenReturn(close);
		when(joinPoint.getArgs()).thenReturn(new Object[] { INDEX_CONTEXT });
		when(joinPoint.proceed()).thenReturn(Boolean.TRUE);
		ruleInterceptor = new RuleInterceptor() {};

		new NonStrictExpectations() {
			{
				action.getRules();
				result = rules;
				action.getRuleExpression();
				result = predicate;
			}
		};
		ACTIONS.put(action.toString(), action);
	}

	@Test
	public void decide() throws Throwable {
		for (final Boolean[] vector : matrix) {

			when(isMultiSearcherInitialised.evaluate(INDEX_CONTEXT)).thenReturn(vector[0]);
			when(areSearchablesInitialised.evaluate(INDEX_CONTEXT)).thenReturn(vector[1]);
			when(isIndexCurrent.evaluate(INDEX_CONTEXT)).thenReturn(vector[2]);
			when(areIndexesCreated.evaluate(INDEX_CONTEXT)).thenReturn(vector[3]);
			when(areUnopenedIndexes.evaluate(INDEX_CONTEXT)).thenReturn(vector[4]);

			Object result = ruleInterceptor.decide(joinPoint);
			Object expected = vector[0] && vector[1] && vector[2] && vector[3] && vector[4];
			String message = Logging.getString("Expected : ", expected, ", result : ", result, " booleans : ", Arrays.asList(vector));
			logger.debug("Result : " + message);
			assertEquals(message, expected, result);
		}
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void decideActions() throws Throwable {
		Mockit.setUpMocks(JEPMock.class);
		for (IAction action : ACTIONS.values()) {
			when(joinPoint.getTarget()).thenReturn(action);
			Object result = ruleInterceptor.decide(joinPoint);
			logger.debug("Result : " + result);
		}
		Mockit.tearDownMocks(JEPMock.class);
	}

	@Test
	public void jep() {
		JEP jep = new JEP();
		jep.addVariable("a", Boolean.FALSE);
		jep.addVariable("b", Boolean.FALSE);
		jep.addVariable("c", Boolean.TRUE);
		jep.addVariable("d", Boolean.TRUE);
		jep.addVariable("e", Boolean.TRUE);
		jep.parseExpression("((a || b) || !c) && (d && e)");
		Object result = jep.getValueAsObject();
		logger.info("Jep result : " + result);
		assertNotNull(result);
	}

}
