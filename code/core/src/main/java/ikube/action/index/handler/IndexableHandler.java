package ikube.action.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RecursiveAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for the handlers that contains access to common functionality like the threads etc.
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

	protected RecursiveAction getRecursiveAction(final IndexContext<?> indexContext, final Indexable<?> indexable, final IResourceProvider<?> resourceManager) {
		class RecursiveActionImpl extends RecursiveAction {

			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			protected void compute() {
				int threadsLeft = indexable.getThreads();
				indexable.setThreads(--threadsLeft);
				if (threadsLeft > 0) {
					logger.info("This : " + this + ", " + indexable.getThreads());
					// Split off some more threads to help do the work
					Indexable<?> leftIndexable = (Indexable<?>) SerializationUtilities.clone(indexable);
					Indexable<?> rightIndexable = (Indexable<?>) SerializationUtilities.clone(indexable);
					RecursiveAction leftRecursiveAction = getRecursiveAction(indexContext, leftIndexable, resourceManager);
					RecursiveAction rightRecursiveAction = getRecursiveAction(indexContext, rightIndexable, resourceManager);
					invokeAll(leftRecursiveAction, rightRecursiveAction);
				}
				Object resource = resourceManager.getResource();
				while (resource != null && !isCancelled()) {
					// Call the handle resource on the parent, which is the implementation specific handler method
					List resources = handleResource(indexContext, indexable, resource);
					// Set any returned resources back in the resource provider
					resourceManager.setResources(resources);
					// Get the next resource from the resource manager, returning null indicates that all the resources are consumed
					resource = resourceManager.getResource();
					ThreadUtilities.sleep(indexContext.getThrottle());
				}
				logger.info("Finished : " + this + ", " + RecursiveAction.getPool().getRunningThreadCount());
			}

		}
		return new RecursiveActionImpl();
	}

	protected abstract List<?> handleResource(final IndexContext<?> indexContext, final Indexable<?> indexable, final Object resource);

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

}