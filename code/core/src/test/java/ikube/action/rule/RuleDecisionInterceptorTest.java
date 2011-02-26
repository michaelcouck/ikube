package ikube.action.rule;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.*;

import ikube.ATest;
import ikube.action.Close;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.toolkit.Permutations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;

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

	private Object[] vector = { Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE };
	private List<Object[]> matrix = new ArrayList<Object[]>();

	@Before
	public void before() throws Throwable {
		new Permutations().getPermutations(vector, matrix, vector.length, 0);

		rules = new ArrayList<IRule<?>>();

		IsMultiSearcherInitialised isMultiSearcherInitialised = mock(IsMultiSearcherInitialised.class);
		rules.add(isMultiSearcherInitialised);

		AreSearchablesInitialised areSearchablesInitialised = mock(AreSearchablesInitialised.class);
		rules.add(areSearchablesInitialised);

		IsIndexCurrent isIndexCurrent = mock(IsIndexCurrent.class);
		rules.add(isIndexCurrent);

		AreIndexesCreated areIndexesCreated = mock(AreIndexesCreated.class);
		rules.add(areIndexesCreated);

		AreUnopenedIndexes areUnopenedIndexes = mock(AreUnopenedIndexes.class);
		rules.add(areUnopenedIndexes);

		close = mock(Close.class);
		when(close.getPredicate()).thenReturn("a && b && c && d && e");
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
		for (Object[] vector : matrix) {
			setMatrix(rules, vector);
			Boolean[] booleans = new Boolean[vector.length];
			System.arraycopy(vector, 0, booleans, 0, booleans.length);
			
			Object result = ruleDecisionInterceptor.decide(joinPoint);
			Object expected = booleans[0] && booleans[1] && !booleans[2] && booleans[3] && booleans[4];
			String message = Logging.getString("Expected : ", expected, ", result : ", result, " booleans : ", Arrays.asList(booleans));
			assertEquals(message, expected, result);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setMatrix(List<IRule<?>> rules, Object[] vector) {
		for (int i = 0; i < rules.size(); i++) {
			IRule rule = rules.get(i);
			when(rule.evaluate(any())).thenReturn((Boolean) vector[i]);
		}
	}

	@Test
	public void jep() throws Exception {
		JEP jep = new JEP();
		jep.addVariable("a", Boolean.FALSE);
		jep.addVariable("b", Boolean.FALSE);
		jep.addVariable("c", Boolean.TRUE);
		jep.addVariable("d", Boolean.TRUE);
		jep.addVariable("e", Boolean.TRUE);
		Node node = jep.parse("((a || b) || !c) && (d && e)");
		Object result = jep.evaluate(node);
		logger.info("Jep result : " + result);
	}

}
