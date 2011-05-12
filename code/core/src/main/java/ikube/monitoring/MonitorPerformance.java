package ikube.monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 22.11.10
 * @version 01.00
 */
public class MonitorPerformance implements IMonitor {

	private class Execution {
		int invocations;
		String name;
		long duration;
	}

	private static final Logger LOGGER = Logger.getLogger(MonitorPerformance.class);
	private transient final Map<String, Execution> executions;
	private transient double invocations = 10000;

	public MonitorPerformance() {
		this.executions = new HashMap<String, MonitorPerformance.Execution>();
	}

	@Override
	public Object monitor(final ProceedingJoinPoint call) throws Throwable {
		String name = call.getSignature().toString();
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
			return call.proceed();
		} finally {
			long stop = System.nanoTime();
			long duration = stop - start;
			execution.duration += duration;
		}
	}

}