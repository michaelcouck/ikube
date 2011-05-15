package ikube.monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @see IMonitoringInterceptor
 * @author Michael Couck
 * @since 08.05.2011
 * @version 01.00
 */
public class MonitoringInterceptor implements IMonitoringInterceptor {

	private class Execution {
		int invocations;
		String name;
		long duration;
	}

	private static final transient Logger LOGGER = Logger.getLogger(MonitoringInterceptor.class);

	private transient final Map<String, Execution> executions;
	private transient double invocations = 10000;
	private transient long searches;
	private transient long duration;

	public MonitoringInterceptor() {
		this.executions = new HashMap<String, Execution>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object indexingPerformance(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		String name = proceedingJoinPoint.getSignature().toString();
		Execution execution = this.executions.get(name);
		if (execution == null) {
			execution = new Execution();
			execution.name = name;
			this.executions.put(execution.name, execution);
		}
		execution.invocations++;
		if (execution.invocations % invocations == 0) {
			long seconds = TimeUnit.NANOSECONDS.toSeconds(execution.duration);
			double executionsPerSecond = execution.invocations / seconds;
			LOGGER.info("Execution : " + execution.invocations + ", duration : " + seconds + ", per second : " + executionsPerSecond
					+ ", name : " + execution.name);
		}
		long start = System.nanoTime();
		try {
			return proceedingJoinPoint.proceed();
		} finally {
			execution.duration += System.nanoTime() - start;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object searchingPerformance(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		// TODO This is very simplistic, what we would like to do is
		// get the index that is being searched and log all the statistics
		// for each index
		searches++;
		long start = System.nanoTime();
		try {
			return proceedingJoinPoint.proceed();
		} finally {
			this.duration += System.nanoTime() - start;
			if (searches % 1000 == 0) {
				LOGGER.error("Search statistics : " + searches + ", duration : " + duration + ", per second : " + searches
						/ TimeUnit.NANOSECONDS.toSeconds(duration));
			}
		}
	}

}