package ikube.action.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.action.Action;
import ikube.action.Close;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.Permutations;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

	@SuppressWarnings("rawtypes")
	private static Map<String, Action> ACTIONS;

	private transient ProceedingJoinPoint joinPoint;
	private transient IRuleInterceptor ruleInterceptor;

	private transient final Boolean[] vector = { Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE };
	private transient final List<Boolean[]> matrix = new ArrayList<Boolean[]>();
	private transient Boolean[] resultVector = { Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE };

	@NonStrict
	private transient IsMultiSearcherInitialised isMultiSearcherInitialised;
	@NonStrict
	private transient AreSearchablesInitialised areSearchablesInitialised;
	@NonStrict
	private transient IsIndexCurrent isIndexCurrent;
	@NonStrict
	private transient AreIndexesCreated areIndexesCreated;
	@NonStrict
	private transient AreUnopenedIndexes areUnopenedIndexes;

	public RuleInterceptorTest() {
		super(RuleInterceptorTest.class);
	}

	@BeforeClass
	public static void beforeClass() {
		File file = FileUtilities.findFileRecursively(new File("."), "spring-actions.xml");
		ApplicationContextManager.getApplicationContext(file);
		ACTIONS = ApplicationContextManager.getBeans(Action.class);
		Mockit.setUpMocks();
		Mockit.setUpMocks(ApplicationContextManagerMock.class);
	}

	@AfterClass
	public static void afterClass() {
		Mockit.tearDownMocks();
		Mockit.tearDownMocks(ApplicationContextManagerMock.class);
		ApplicationContextManager.closeApplicationContext();
	}

	@Before
	public void before() throws Throwable {
		new Permutations().getPermutations(vector, matrix, vector.length, 0);

		new NonStrictExpectations() {
			{
				isMultiSearcherInitialised.evaluate(INDEX_CONTEXT);
				result = resultVector[0];
				areSearchablesInitialised.evaluate(INDEX_CONTEXT);
				result = resultVector[1];
				isIndexCurrent.evaluate(INDEX_CONTEXT);
				result = resultVector[2];
				areIndexesCreated.evaluate(INDEX_CONTEXT);
				result = resultVector[3];
				areUnopenedIndexes.evaluate(INDEX_CONTEXT);
				result = resultVector[4];
			}
		};

		List<IRule<IndexContext>> rules = new ArrayList<IRule<IndexContext>>();
		rules.add(isMultiSearcherInitialised);
		rules.add(areSearchablesInitialised);
		rules.add(isIndexCurrent);
		rules.add(areIndexesCreated);
		rules.add(areUnopenedIndexes);

		Close close = mock(Close.class);
		String predicate = "IsMultiSearcherInitialised && AreSearchablesInitialised && !IsIndexCurrent && AreIndexesCreated && AreUnopenedIndexes";
		when(close.getPredicate()).thenReturn(predicate);
		when(close.getRules()).thenReturn(rules);
		when(close.execute(any(IndexContext.class))).thenReturn(Boolean.TRUE);

		joinPoint = mock(ProceedingJoinPoint.class);
		when(joinPoint.getTarget()).thenReturn(close);
		when(joinPoint.getArgs()).thenReturn(new Object[] { INDEX_CONTEXT });
		when(joinPoint.proceed()).thenReturn(Boolean.TRUE);
		ruleInterceptor = new RuleInterceptor();
	}

	@Test
	public void decide() throws Throwable {
		for (Boolean[] vector : matrix) {
			resultVector = vector;
			Object result = ruleInterceptor.decide(joinPoint);
			Object expected = resultVector[0] && resultVector[1] && !resultVector[2] && resultVector[3] && resultVector[4];
			String message = Logging.getString("Expected : ", expected, ", result : ", result, " booleans : ", Arrays.asList(resultVector));
			assertEquals(message, expected, result);
		}
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void decideMulti() throws Throwable {
		/**
		 * TODO Complete this test!
		 * 
		 * <pre>
		 * 		<ref bean="ikube.action.Reset" /> 
		 * 		<ref bean="ikube.action.Close"  />
		 * 		<ref bean="ikube.action.Open"  />
		 * 		<ref bean="ikube.action.Index"  />
		 * 		<ref bean="ikube.action.Delete" />
		 * 		<ref bean="ikube.action.Clean" />
		 * </pre>
		 * 
		 * First check if he executed the actions in various combinations, cases:
		 * 
		 * <pre>
		 * 1) Index doesn't exist - should execute all actions: 
		 *     Actions: reset, index, delete, clean
		 * 2) Index exists and is current - should execute the delete and the reset
		 *     Actions: reset, open, delete, clean 
		 * 3) Index exists but is not current - should execute all actions
		 *     Actions: reset, index, delete, clean
		 * 4) Index exists but is not current and other servers working - should execute the index
		 *     Actions: reset, index, delete, clean
		 * </pre>
		 * 
		 * Cluster cases:
		 * 
		 * <pre>
		 * 1) No servers working:
		 * 		Actions: reset, index, delete, clean
		 * 2) Servers working and index not current:
		 * 		Actions: index, delete, clean
		 * </pre>
		 */
		for (Action action : ACTIONS.values()) {
			when(joinPoint.getTarget()).thenReturn(action);
			Object result = ruleInterceptor.decide(joinPoint);
			logger.info("Result : " + result);
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
		jep.parseExpression("((a || b) || !c) && (d && e)");
		Object result = jep.getValueAsObject();
		logger.info("Jep result : " + result);
		assertNotNull(result);
	}

}
