package ikube.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 18.01.12
 * @version 01.00
 */
public interface IActionInterceptor {

	boolean preProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable;

	boolean postProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable;

}
