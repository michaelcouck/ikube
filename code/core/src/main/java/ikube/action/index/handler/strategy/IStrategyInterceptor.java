package ikube.action.index.handler.strategy;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * This is the interceptor for the file system and data base handlers. Essentially this interceptor will execute strategies on the handlers before processing
 * and based on the result allow the handler to proceed to index the resource or not. This facilitates delta indexing and adding data to the document before
 * committing the data to the index, like adding a file while processing the database.
 * 
 * @author Michael Couck
 * @since 27.12.12
 * @version 01.00
 */
public interface IStrategyInterceptor {

	/**
	 * This method will intercept the handler 'handle' methods, and implementors will then execute strategies that are contained in the contexts of the
	 * proceeding join point.
	 * 
	 * @param proceedingJoinPoint the join point for the handler
	 * @return the result from executing the join point, or null if the implementing logic decides not to execute the intercepted target
	 * @throws Throwable
	 */
	Object aroundProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable;

}
