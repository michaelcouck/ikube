package ikube.action.rule;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * This is the interface for intercepting actions and deciding whether to execute the actions. Typically sub-classes will check whether
 * there are indexes being built or have been built and execute the actions based on the category of the evaluation of the rules. Rules are
 * defined in the Spring configuration.
 * 
 * @author Michael Couck
 * @since 12-02-2011
 * @version 01.00
 */
public interface IRuleInterceptor {

	/**
	 * This method intercepts the actions. If the rules that are configured for the action evaluate to true then the action is executed,
	 * i.e. the join point proceeds, if not then the return value is just a boolean.
	 * 
	 * @param call the join point where the intercepter will intercept the call to the actions
	 * @return the object resulting from the call to the target action or a boolean
	 * @throws Throwable
	 */
	Object decide(final ProceedingJoinPoint call) throws Throwable;

}
