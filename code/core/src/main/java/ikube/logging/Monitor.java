package ikube.logging;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 22.11.10
 * @version 01.00
 */
public class Monitor implements IMonitor {

	private class Execution {
		int invocations;
		String name;
		double start;
		double stop;
	}

	private static final Logger LOGGER = Logger.getLogger(Monitor.class);
	private transient final Map<String, Execution> executions;
	private transient double invocations = 10;

	public Monitor() {
		this.executions = new HashMap<String, Monitor.Execution>();
	}

	@Override
	public Object monitor(final ProceedingJoinPoint call) throws Throwable {
		String name = call.getSignature().toString();
		Execution execution = this.executions.get(name);
		if (execution == null) {
			execution = new Execution();
			execution.name = name;
			execution.start = System.currentTimeMillis();
			this.executions.put(execution.name, execution);
		}
		execution.invocations++;
		if (execution.invocations % invocations == 0) {
			execution.stop = System.currentTimeMillis();
			double duration = execution.stop - execution.start;
			double executionsPerSecond = (execution.invocations / duration) / 1000d;
			LOGGER.info("Execution : " + execution.invocations + ", duration : " + duration + ", per second : " + executionsPerSecond
					+ ", name : " + execution.name);
		}
		return call.proceed();
	}

}