package ikube.action.index.handler;

import ikube.model.Indexable;

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

	/** The number of threads that this handler will spawn. */
	private int threads;
	/** The class that this handler can handle. */
	private Class<T> indexableClass;
	/** A local storage for the maximum exceptions per thread. */
	private ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>();

	public int getThreads() {
		return threads;
	}

	public void setThreads(final int threads) {
		this.threads = threads;
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

	protected void handleMaxExceptions(final Indexable<?> indexable, final Exception exception) {
		if (threadLocal.get() == null) {
			threadLocal.set(new Integer(0));
		}
		threadLocal.set(threadLocal.get() + 1);
		if (threadLocal.get() > indexable.getMaxExceptions()) {
			throw new RuntimeException("Maximum exceptions exceeded for resource : ", exception);
		}
	}

}