package ikube.aspect;

import ikube.model.Execution;

import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 22.11.10
 * @version 01.00
 */
public interface IMonitor {

	public Object monitor(ProceedingJoinPoint call) throws Throwable;

	public Map<String, Execution> getExecutions();

}
