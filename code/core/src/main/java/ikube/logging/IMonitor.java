package ikube.logging;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public interface IMonitor {

	public Object monitor(ProceedingJoinPoint call) throws Throwable;
	
}
