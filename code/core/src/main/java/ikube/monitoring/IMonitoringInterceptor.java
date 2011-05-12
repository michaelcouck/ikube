package ikube.monitoring;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 08.05.2011
 * @version 01.00
 */
public interface IMonitoringInterceptor {

	Object indexingPerformance(ProceedingJoinPoint call) throws Throwable;
	
	Object searchingPerformance(ProceedingJoinPoint call) throws Throwable;

}
