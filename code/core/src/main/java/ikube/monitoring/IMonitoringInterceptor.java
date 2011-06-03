package ikube.monitoring;

import ikube.listener.IListener;
import ikube.model.Execution;

import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * This intercepter will intercept the important points in the indexing process, namely the addDocument method and the search methods in the
 * search classes. It will record the times taken for the methods to be invoked and make these available in the server object which can then
 * be published to the UI.
 * 
 * @author Michael Couck
 * @since 08.05.2011
 * @version 01.00
 */
public interface IMonitoringInterceptor extends IListener {

	/**
	 * Accesses the search executions map. Each map is a map of the name of the index as the key and the execution for that index as the
	 * value.
	 * 
	 * @return the search executions map
	 */
	Map<String, Execution> getSearchingExecutions();

	/**
	 * Accesses the indexing executions map. As in the search executions map, the map is based on the index name as the key and the
	 * execution for that search execution as the value.
	 * 
	 * @return the indexing executions map
	 */
	Map<String, Execution> getIndexingExecutions();

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
	 * This is the aspect method that will intercept the search calls, the search classes like SearchSingle will use a SearchDelegate. This
	 * delegate is then managed by Spring and there is a public method specifically used that we can catch the call and measure it.
	 * Unfortunately Spring AOP is not as powerful as AspectJ, but more elegant and easier to use, i.e. less configuration.
	 * 
	 * @param proceedingJoinPoint
	 *            the join point for the method call
	 * @return whatever the return from the join point is
	 * @throws Throwable
	 *             if anything happens then the exception bubbles up the stack
	 */
	Object searchingPerformance(ProceedingJoinPoint proceedingJoinPoint) throws Throwable;

}
