package ikube.statistics;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 07.07.13
 * @version 01.00
 */
public interface ITimerInterceptor {

	Object aroundProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable;

}
