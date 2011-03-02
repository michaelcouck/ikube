package ikube.action.rule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.action.Close;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.toolkit.Permutations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class RuleDecisionInterceptorTest extends ATest {

	private Close close;
	private List<IRule<?>> rules;
	private ProceedingJoinPoint joinPoint;
	private IRuleDecisionInterceptor ruleDecisionInterceptor;

	private Boolean[] vector = { Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE };
	private List<Boolean[]> matrix = new ArrayList<Boolean[]>();
	private Boolean[] resultVector = { Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE };

	@NonStrict
	private IsMultiSearcherInitialised isMultiSearcherInitialised;
	@NonStrict
	private AreSearchablesInitialised areSearchablesInitialised;
	@NonStrict
	private IsIndexCurrent isIndexCurrent;
	@NonStrict
	private AreIndexesCreated areIndexesCreated;
	@NonStrict
	private AreUnopenedIndexes areUnopenedIndexes;

	@BeforeClass
	public static void beforeClass() {
		Mockit.setUpMocks();
	}

	@AfterClass
	public static void afterClass() {
		Mockit.tearDownMocks();
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

		rules = new ArrayList<IRule<?>>();
		rules.add(isMultiSearcherInitialised);
		rules.add(areSearchablesInitialised);
		rules.add(isIndexCurrent);
		rules.add(areIndexesCreated);
		rules.add(areUnopenedIndexes);

		close = mock(Close.class);
		String predicate = "IsMultiSearcherInitialised && AreSearchablesInitialised && !IsIndexCurrent && AreIndexesCreated && AreUnopenedIndexes";
		when(close.getPredicate()).thenReturn(predicate);
		when(close.getRules()).thenReturn(rules);
		when(close.execute(any(IndexContext.class))).thenReturn(Boolean.TRUE);

		joinPoint = mock(ProceedingJoinPoint.class);
		when(joinPoint.getTarget()).thenReturn(close);
		when(joinPoint.getArgs()).thenReturn(new Object[] { INDEX_CONTEXT });
		when(joinPoint.proceed()).thenReturn(Boolean.TRUE);
		ruleDecisionInterceptor = new RuleDecisionInterceptor();
	}

	@Test
	public void decide() throws Throwable {
		for (Boolean[] vector : matrix) {
			resultVector = vector;
			Object result = ruleDecisionInterceptor.decide(joinPoint);
			Object expected = resultVector[0] && resultVector[1] && !resultVector[2] && resultVector[3] && resultVector[4];
			String message = Logging.getString("Expected : ", expected, ", result : ", result, " booleans : ", Arrays.asList(resultVector));
			assertEquals(message, expected, result);
		}
	}

	@Test
	public void jep() throws Exception {
		JEP jep = new JEP();
		jep.addVariableAsObject("a", Boolean.FALSE);
		jep.addVariableAsObject("b", Boolean.FALSE);
		jep.addVariableAsObject("c", Boolean.TRUE);
		jep.addVariableAsObject("d", Boolean.TRUE);
		jep.addVariableAsObject("e", Boolean.TRUE);
		jep.parseExpression("((a || b) || !c) && (d && e)");
		Object result = jep.getValueAsObject();
		logger.info("Jep result : " + result);
	}

}
