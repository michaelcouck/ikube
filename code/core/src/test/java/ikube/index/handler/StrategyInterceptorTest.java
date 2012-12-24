package ikube.index.handler;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.model.Indexable;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class StrategyInterceptorTest extends ATest {

	private StrategyInterceptor strategyInterceptor;

	public StrategyInterceptorTest() {
		super(StrategyInterceptorTest.class);
	}

	@Before
	public void before() {
		strategyInterceptor = new StrategyInterceptor();
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void preProcess() throws Throwable {
		ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
		Indexable indexable = Mockito.mock(Indexable.class);
		IStrategy strategy = Mockito.mock(IStrategy.class);
		Mockito.when(strategy.preProcess(Mockito.any())).thenReturn(Boolean.TRUE);
		Mockito.when(indexable.getStrategies()).thenReturn(Arrays.asList(strategy));
		Object[] args = new Object[] { indexable };
		Mockito.when(proceedingJoinPoint.getArgs()).thenReturn(args);
		strategyInterceptor.preProcess(proceedingJoinPoint);

		Mockito.verify(strategy, Mockito.atLeastOnce()).preProcess(Mockito.any());
		Mockito.verify(proceedingJoinPoint, Mockito.atLeastOnce()).proceed((Object[]) Mockito.any());

		Mockito.when(proceedingJoinPoint.proceed((Object[]) Mockito.any())).thenReturn(Boolean.TRUE);
		Object result = strategyInterceptor.preProcess(proceedingJoinPoint);
		assertTrue(Boolean.TRUE.equals(result));

		IStrategy strategyFail = Mockito.mock(IStrategy.class);
		Mockito.when(strategyFail.preProcess(Mockito.any())).thenReturn(Boolean.FALSE);
		Mockito.when(indexable.getStrategies()).thenReturn(Arrays.asList(strategy, strategyFail));

		result = strategyInterceptor.preProcess(proceedingJoinPoint);
		assertNull(result);
	}

}
