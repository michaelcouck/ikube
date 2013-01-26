package ikube.interceptor;

import ikube.ATest;

import org.junit.Before;
import org.junit.Test;

@Deprecated
public class StrategyInterceptorTest extends ATest {

	@SuppressWarnings("unused")
	private StrategyInterceptor strategyInterceptor;

	public StrategyInterceptorTest() {
		super(StrategyInterceptorTest.class);
	}

	@Before
	public void before() {
		strategyInterceptor = new StrategyInterceptor();
	}

	@Test
	public void preProcess() throws Throwable {
		// ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
		// Indexable indexable = Mockito.mock(Indexable.class);
		// IStrategy strategy = Mockito.mock(IStrategy.class);
		// Mockito.when(strategy.aroundProcess(Mockito.any())).thenReturn(Boolean.TRUE);
		// Mockito.when(indexable.getStrategies()).thenReturn(Arrays.asList(strategy));
		// Object[] args = new Object[] { indexable };
		// Mockito.when(proceedingJoinPoint.getArgs()).thenReturn(args);
		// strategyInterceptor.aroundProcess(proceedingJoinPoint);
		//
		// Mockito.verify(strategy, Mockito.atLeastOnce()).aroundProcess(Mockito.any());
		// Mockito.verify(proceedingJoinPoint, Mockito.atLeastOnce()).proceed((Object[]) Mockito.any());
		//
		// Mockito.when(proceedingJoinPoint.proceed((Object[]) Mockito.any())).thenReturn(Boolean.TRUE);
		// Object result = strategyInterceptor.aroundProcess(proceedingJoinPoint);
		// assertTrue(Boolean.TRUE.equals(result));
		//
		// IStrategy strategyFail = Mockito.mock(IStrategy.class);
		// Mockito.when(strategyFail.aroundProcess(Mockito.any())).thenReturn(Boolean.FALSE);
		// Mockito.when(indexable.getStrategies()).thenReturn(Arrays.asList(strategy, strategyFail));
		//
		// result = strategyInterceptor.aroundProcess(proceedingJoinPoint);
		// assertNull(result);
	}

}
