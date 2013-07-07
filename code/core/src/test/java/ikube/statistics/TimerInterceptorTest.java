package ikube.statistics;

import ikube.AbstractTest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TimerInterceptorTest extends AbstractTest {

	private TimerInterceptor timerInterceptor;

	@Before
	public void before() {
		timerInterceptor = new TimerInterceptor();
	}

	@Test
	public void aroundProcess() throws Throwable {
		ProceedingJoinPoint proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
		timerInterceptor.aroundProcess(proceedingJoinPoint);
		Mockito.verify(proceedingJoinPoint, Mockito.atLeastOnce()).proceed();

		proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
		timerInterceptor.aroundProcess(proceedingJoinPoint);
		Mockito.verify(proceedingJoinPoint, Mockito.atMost(1)).proceed();

		proceedingJoinPoint = Mockito.mock(ProceedingJoinPoint.class);
		timerInterceptor.aroundProcess(proceedingJoinPoint);
		Mockito.verify(proceedingJoinPoint, Mockito.atMost(1)).proceed();
	}

}
