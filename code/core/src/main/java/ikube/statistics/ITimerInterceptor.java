package ikube.statistics;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;

/**
 * @author Michael Couck
 * @since 07.07.13
 * @version 01.00
 */
@Service
public interface ITimerInterceptor {

	Object aroundProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable;

}
