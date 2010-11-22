package ikube.aspect;

import ikube.model.Execution;

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

	private Logger logger = Logger.getLogger(this.getClass());
	private Map<String, Execution> executions;

	public Monitor() {
		this.executions = new HashMap<String, Execution>();
	}

	@Override
	public Object monitor(ProceedingJoinPoint call) throws Throwable {
		String name = call.getSignature().toString();
		Execution execution = this.executions.get(name);
		if (execution == null) {
			execution = new Execution();
			execution.setName(name);
			this.executions.put(name, execution);
		}
		execution.setExecutions(execution.getExecutions() + 1);
		logger.debug("Monitor : " + executions);
		return call.proceed();
	}

	public Map<String, Execution> getExecutions() {
		return executions;
	}

}
