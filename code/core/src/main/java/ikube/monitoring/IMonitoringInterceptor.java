package ikube.monitoring;

import ikube.listener.IListener;
import ikube.model.Execution;

import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 08.05.2011
 * @version 01.00
 */
public interface IMonitoringInterceptor extends IListener {

	Map<String, Execution> getSearchingExecutions();

	Map<String, Execution> getIndexingExecutions();

	Object indexingPerformance(ProceedingJoinPoint call) throws Throwable;

	Object searchingPerformance(ProceedingJoinPoint call) throws Throwable;

}
