package ikube.action.rule;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.action.index.handler.IStrategy;
import ikube.action.index.handler.strategy.StrategyInterceptor;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.util.Arrays;

import mockit.Cascading;
import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 27.12.12
 * @version 01.00
 */
@Deprecated
public class StrategyInterceptorTest extends ATest {

	@Cascading
	private Document document;
	@Cascading
	@SuppressWarnings("rawtypes")
	private IndexContext indexContext;
	private Object resource = new Object();

	@SuppressWarnings("rawtypes")
	private Indexable indexable = Mockito.mock(Indexable.class);
	private IStrategy strategy = Mockito.mock(IStrategy.class);

	/** Class under test. */
	private StrategyInterceptor strategyInterceptor;

	public StrategyInterceptorTest() {
		super(StrategyInterceptorTest.class);
	}

	@Before
	public void before() {
		strategyInterceptor = new StrategyInterceptor();
		Mockit.setUpMocks();
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void preProcess() throws Throwable {
		Object[] args = new Object[] { indexContext, indexable, document, resource };
		ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);

		Mockito.when(strategy.aroundProcess(indexContext, indexable, document, resource)).thenReturn(Boolean.TRUE);
		Mockito.when(indexable.getStrategies()).thenReturn(Arrays.asList(strategy));
		Mockito.when(proceedingJoinPoint.getArgs()).thenReturn(args);
		strategyInterceptor.aroundProcess(proceedingJoinPoint);

		Mockito.verify(strategy, Mockito.atLeastOnce()).aroundProcess(indexContext, indexable, document, resource);
		Mockito.verify(proceedingJoinPoint, Mockito.atLeastOnce()).proceed((Object[]) Mockito.any());

		Mockito.when(proceedingJoinPoint.proceed((Object[]) Mockito.any())).thenReturn(Boolean.TRUE);
		Object result = strategyInterceptor.aroundProcess(proceedingJoinPoint);
		assertTrue(Boolean.TRUE.equals(result));

		IStrategy strategyFail = Mockito.mock(IStrategy.class);
		Mockito.when(strategyFail.aroundProcess(indexContext, indexable, document, resource)).thenReturn(Boolean.FALSE);
		Mockito.when(indexable.getStrategies()).thenReturn(Arrays.asList(strategy, strategyFail));

		result = strategyInterceptor.aroundProcess(proceedingJoinPoint);
		assertNull(result);
	}

}
