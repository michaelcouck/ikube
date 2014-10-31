package ikube.action.rule;

import ikube.AbstractTest;
import ikube.action.index.handler.IStrategy;
import ikube.action.index.handler.strategy.StrategyInterceptor;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import mockit.Cascading;
import mockit.Mockit;
import org.apache.lucene.document.Document;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 27-12-2012
 */
@SuppressWarnings("UnusedDeclaration")
public class StrategyInterceptorTest extends AbstractTest {

	@Cascading
	private Document document;
	@Cascading
	@SuppressWarnings("rawtypes")
	private IndexContext indexContext;
	private Object resource = new Object();

	@SuppressWarnings("rawtypes")
	private Indexable indexable = Mockito.mock(Indexable.class);
	private IStrategy strategy = Mockito.mock(IStrategy.class);

	/**
	 * Class under test.
	 */
	private StrategyInterceptor strategyInterceptor;

	@Before
	public void before() {
		strategyInterceptor = new StrategyInterceptor();
		Mockit.setUpMocks();
	}

	@Test
	public void preProcess() throws Throwable {
		Object[] args = new Object[]{indexContext, indexable, document, resource};
		ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);

		Mockito.when(strategy.aroundProcess(indexContext, indexable, document, resource)).thenReturn(Boolean.TRUE);
		Mockito.when(indexable.getStrategies()).thenReturn(Arrays.asList(strategy));
		Mockito.when(proceedingJoinPoint.getArgs()).thenReturn(args);
		strategyInterceptor.aroundProcess(proceedingJoinPoint);

		Mockito.verify(strategy, Mockito.atLeastOnce()).aroundProcess(indexContext, indexable, document, resource);
		Mockito.verify(proceedingJoinPoint, Mockito.atLeastOnce()).proceed();

		Mockito.when(proceedingJoinPoint.proceed()).thenReturn(Boolean.TRUE);
		Object result = strategyInterceptor.aroundProcess(proceedingJoinPoint);
		assertTrue(Boolean.TRUE.equals(result));

		IStrategy strategyFail = Mockito.mock(IStrategy.class);
		Mockito.when(strategyFail.aroundProcess(indexContext, indexable, document,
            resource)).thenReturn(Boolean.FALSE);
		Mockito.when(indexable.getStrategies()).thenReturn(Arrays.asList(strategy, strategyFail));

		result = strategyInterceptor.aroundProcess(proceedingJoinPoint);
		assertEquals(Boolean.FALSE, result);
	}

}
