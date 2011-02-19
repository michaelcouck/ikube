package ikube.action.rule;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public interface IRuleDecisionInterceptor {

	public Object decide(ProceedingJoinPoint call) throws Throwable;

}
