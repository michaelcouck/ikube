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
 * Base class for the handlers that contains access to common functionality like the threads etc.
 * 
 * @see IIndexableHandler
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public abstract class IndexableHandler<T extends Indexable<?>> implements IIndexableHandler<T> {

	public static final String RESOURCE_HANDLER_QUALIFIER = ResourceHandler.class.getName();

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	/** The class that this handler can handle. */
	private Class<T> indexableClass;
	@Autowired
	@Qualifier("ikube.action.index.handler.ResourceHandler")
	protected ResourceHandler<T> resourceHandler;

	protected RecursiveAction getRecursiveAction(final IndexContext<?> indexContext, final T indexable, final IResourceProvider<?> resourceManager) {
		/**
		 * This class will execute the handle resource on the handler until there are no more resources left or until it is cancelled.
		 */
		class RecursiveActionImpl extends RecursiveAction {

			/**
			 * This method is the implementation from {@link RecursiveAction} that calls the handle {@link IndexableHandler#handleResource(...) } on the sub
			 * classes.
			 */
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			protected void compute() {
				// Lets see if we should split off some threads
				computeRecursive();
				do {
					Object resource = resourceManager.getResource();
					if (resource == null || isCancelled() || isDone() || isCompletedNormally() || isCompletedAbnormally()) {
						break;
					}
					// Call the handle resource on the parent, which is the implementation specific handler method
					List resources = handleResource(indexContext, indexable, resource);
					// Set any returned resources back in the resource provider
					resourceManager.setResources(resources);
					// Sleep for the required time, zzzzzz.....
					ThreadUtilities.sleep(indexContext.getThrottle());
				} while (true);
				logger.info("Finished : " + this + ", " + RecursiveAction.getPool().getRunningThreadCount());
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			private void computeRecursive() {
				if (indexable.incrementThreads(-1) >= 0) {
					logger.info("This : " + this + ", " + indexable.getThreads());
					// Split off some more threads to help do the work
					T leftIndexable = (T) SerializationUtilities.clone(indexable);
					T rightIndexable = (T) SerializationUtilities.clone(indexable);
					((Indexable) leftIndexable).setStrategies(indexable.getStrategies());
					((Indexable) rightIndexable).setStrategies(indexable.getStrategies());
					
					RecursiveAction leftRecursiveAction = getRecursiveAction(indexContext, leftIndexable, resourceManager);
					RecursiveAction rightRecursiveAction = getRecursiveAction(indexContext, rightIndexable, resourceManager);
					invokeAll(leftRecursiveAction, rightRecursiveAction);
				}
			}

		}
		// And hup
		return new RecursiveActionImpl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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

	protected void handleException(final Indexable<?> indexable, final Exception exception, final String... messages) {
		if (InterruptedException.class.isAssignableFrom(exception.getClass()) || CancellationException.class.isAssignableFrom(exception.getClass())) {
			throw new RuntimeException("Worker thread interrupted : " + Arrays.deepToString(messages), exception);
		}
		indexable.setExceptions(indexable.getExceptions() + 1);
		if (indexable.getExceptions() > indexable.getMaxExceptions()) {
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