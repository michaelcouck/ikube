package ikube.monitoring;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * This intercepter will intercept the important points in the indexing process, namely the addDocument method and the
 * search methods in the search classes. It will record the times taken for the methods to be invoked and make these
 * available in the server object which can then be published to the UI.
 * 
 * @author Michael Couck
 * @since 08.05.2011
 * @version 01.00
 */
public interface IMonitoringInterceptor {

	/**
	 * This is the aspect method that will intercept the indexing call, i.e. the addDocument method in the delegate.
	 * 
	 * @param proceedingJoinPoint
	 *            the join point for the method call
	 * @return whatever the return from the join point is
	 * @throws Throwable
	 *             if anything happens then the exception bubbles up the stack
	 */
	Object indexingPerformance(ProceedingJoinPoint proceedingJoinPoint) throws Throwable;

	/**
	 * This is the aspect method that will intercept the search calls, the search classes like SearchSingle will use a
	 * SearchDelegate. This delegate is then managed by Spring and there is a public method specifically used that we
	 * can catch the call and measure it. Unfortunately Spring AOP is not as powerful as AspectJ, but more elegant and
	 * easier to use, i.e. less configuration.
	 * 
	 * @param proceedingJoinPoint
	 *            the join point for the method call
	 * @return whatever the return from the join point is
	 * @throws Throwable
	 *             if anything happens then the exception bubbles up the stack
	 */
	Object searchingPerformance(ProceedingJoinPoint proceedingJoinPoint) throws Throwable;

}
