package ikube.aspect;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 22.11.10
 * @version 01.00
 */
public interface IProfile {

	public Object profile(ProceedingJoinPoint call) throws Throwable;

}