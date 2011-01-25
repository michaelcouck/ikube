package ikube.logging;

import org.aspectj.lang.ProceedingJoinPoint;

public interface IMonitor {

	public Object monitor(ProceedingJoinPoint call) throws Throwable;
	
}
