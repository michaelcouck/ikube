package ikube.index.handler.strategy;

import ikube.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;

/**
 * TODO Comments...
 * 
 * @author Michael Couck
 * @since 12.12.12
 * @version 01.00
 */
public abstract class AStrategy implements IStrategy {

	/** The next strategy in the chain. */
	IStrategy nextStrategy;

	/**
	 * Constructor takes the next strategy, could be null.
	 * 
	 * @param next the chained strategy to execute
	 */
	public AStrategy(final IStrategy nextStrategy) {
		this.nextStrategy = nextStrategy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean preProcess(final Object... parameters) {
		if (nextStrategy != null) {
			return nextStrategy.preProcess(parameters);
		}
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean postProcess(final Object... parameters) {
		if (nextStrategy != null) {
			return nextStrategy.postProcess(parameters);
		}
		return Boolean.TRUE;
	}

	protected IndexContext<?> getIndexContext(final Indexable<?> indexable) {
		if (IndexContext.class.isAssignableFrom(indexable.getClass())) {
			return (IndexContext<?>) indexable;
		}
		return getIndexContext(indexable.getParent());
	}

}