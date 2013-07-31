package ikube.action.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Base class for the handlers that contains access to common functionality like the threads etc. This class also contains the logic to split off tasks to be
 * executed in parallel, over several threads, distributing the work load evenly over the cores.
 * 
 * @see IIndexableHandler
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public abstract class IndexableHandler<T extends Indexable<?>> implements IIndexableHandler<T> {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	/** The class that this handler can handle. */
	private Class<T> indexableClass;
	/** This is the 'generic' handler for the resource, it just adds the document to the index writer. */
	@Autowired
	@Qualifier("ikube.action.index.handler.ResourceHandler")
	protected ResourceHandler<T> resourceHandler;

	/**
	 * This method will return the recursive action. A {@link RecursiveAction} is one that will potentially spawn off more actions/tasks in an effort to
	 * distribute the load of processing the data over multiple threads, optimizing the cpu. Depending on the number of threads defined in the {@link Indexable}
	 * , so many tasks will be created and invoked.
	 * 
	 * The action will iterate continuously until either there are no more resources available from the resource provider, or the thread pool has been
	 * terminated explicitly. A {@link IResourceProvider} must be passed to the action. This provider will be queried for a resource, which will then be fed
	 * into the handler method {@link IndexableHandler#handleResource(IndexContext, Indexable, Object)}. The handler is then expected to co-ordinate the
	 * resource and the {@link ResourceHandler}, buy possibly processing the the resource and then feeding the data into the resource handler.
	 * 
	 * @param indexContext the index context that will be used to create the tasks
	 * @param indexable the indexable that is being indexed currently
	 * @param resourceProvider the resource provider for the type of handler, internet or database for example
	 * @return the recursive action that has already spawned off the sub tasks, if necessary
	 */
	protected ForkJoinTask<?> getRecursiveAction(final IndexContext<?> indexContext, final T indexable, final IResourceProvider<?> resourceProvider) {
		class RecursiveActionImpl extends RecursiveAction {
			/** @see IndexableHandler#getRecursiveAction(IndexContext, T, IResourceProvider) */
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			protected void compute() {
				// Lets see if we should split off some threads
				computeRecursive(indexContext, indexable, resourceProvider);
				do {
					// When there are no more resources we exit this task
					Object resource = resourceProvider.getResource();
					if (resource == null || isCancelled() || isDone() || isCompletedNormally() || isCompletedAbnormally()) {
						break;
					}
					// Call the handle resource on the parent, which is the implementation specific handler method
					List resources = handleResource(indexContext, indexable, resource);
					// Set any returned resources back in the resource provider, like a feed back mechanism
					resourceProvider.setResources(resources);
					// Sleep for the required time
					ThreadUtilities.sleep(indexContext.getThrottle());
				} while (true);
				logger.info("Finished : " + this + ", " + RecursiveAction.getPool().getRunningThreadCount());
			}
		}
		// And hup
		return new RecursiveActionImpl();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void computeRecursive(final IndexContext<?> indexContext, final T indexable, final IResourceProvider<?> resourceProvider) {
		if (indexable.incrementThreads(-2) <= 0) {
			return;
		}
		logger.info("This : " + this + ", " + indexable.getThreads());
		// Split off some more threads to help do the work
		T leftIndexable = (T) SerializationUtilities.clone(indexable);
		T rightIndexable = (T) SerializationUtilities.clone(indexable);
		((Indexable) leftIndexable).setStrategies(indexable.getStrategies());
		((Indexable) rightIndexable).setStrategies(indexable.getStrategies());

		ForkJoinTask<?> leftRecursiveAction = getRecursiveAction(indexContext, leftIndexable, resourceProvider);
		ForkJoinTask<?> rightRecursiveAction = getRecursiveAction(indexContext, rightIndexable, resourceProvider);
		ForkJoinTask.invokeAll(leftRecursiveAction, rightRecursiveAction);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public List<Future<?>> handleIndexable(final IndexContext<?> indexContext, final T indexable) throws Exception {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ForkJoinTask<?> handleIndexableForked(final IndexContext<?> indexContext, final T indexable) throws Exception {
		return null;
	}

	/**
	 * This method is called from the fork join actions, individually processing a resource, thread by thread.
	 * 
	 * @param indexContext the index context being processed
	 * @param indexable the currently processed indexable
	 * @param resource the resource that is to be processed
	 * @return the list of additional resources collected by the processing of the resource, can be empty or null
	 */
	protected abstract List<?> handleResource(final IndexContext<?> indexContext, final T indexable, final Object resource);

	/**
	 * This method will log any exceptions, and increment the number of exceptions experienced by the handler. If the maximum number of exceptions by the
	 * handler has been exceeded then the exception is re-thrown as a runtime exception. The calling code is then expected to bubble the exception up to the
	 * executing thread/task, that is then expected to terminate execution.
	 * 
	 * @param indexable the indexable that is currently being indexed
	 * @param exception the exception thrown, if this is an interrupted exception or a cancellation exception then we re-throw it immediately. Having said that
	 *        there are times when such exceptions are not thrown by ikube internally, but by Hazelcast and even Lucene, and these also halt the execution
	 * @param messages
	 */
	protected void handleException(final Indexable<?> indexable, final Exception exception, final String... messages) {
		if (InterruptedException.class.isAssignableFrom(exception.getClass()) || CancellationException.class.isAssignableFrom(exception.getClass())) {
			throw new RuntimeException("Worker thread interrupted : " + Arrays.deepToString(messages), exception);
		}
		if (indexable.incrementAndGetExceptions() > indexable.getMaxExceptions()) {
			throw new RuntimeException("Maximum exceptions exceeded for resource : " + indexable.getName() + ", " + Arrays.deepToString(messages), exception);
		}
		logger.error("Exception handling resource : " + Arrays.deepToString(messages), exception);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<T> getIndexableClass() {
		return indexableClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIndexableClass(final Class<T> indexableClass) {
		this.indexableClass = indexableClass;
	}

}