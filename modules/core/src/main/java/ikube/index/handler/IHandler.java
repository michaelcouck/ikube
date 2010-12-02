package ikube.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.util.List;

/**
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public interface IHandler<T extends Indexable<?>> {

	/**
	 * This method executes the handler logic. The method returns a list of threads(if it is multi-threaded) that the caller must wait for.
	 * Once all the threads are dead then the handler's logic is complete.
	 * 
	 * @param indexContext
	 *            the index context for the index
	 * @param indexable
	 *            the indexable that the handler must handle
	 * @return the list of threads that the caller must wait for
	 * @throws Exception
	 */
	public List<Thread> handle(IndexContext indexContext, T indexable) throws Exception;

}
