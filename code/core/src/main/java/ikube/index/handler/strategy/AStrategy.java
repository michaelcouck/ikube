package ikube.index.handler.strategy;

import ikube.index.handler.IStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO Comments...
 * 
 * @author Michael Couck
 * @since 12.12.12
 * @version 01.00
 */
public abstract class AStrategy<T, U> implements IStrategy<T, U> {

	protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	/** The next strategy in the chain. */
	IStrategy<T, U> nextStrategy;

	/**
	 * Constructor takes the next strategy, could be null.
	 * 
	 * @param next the chained strategy to execute
	 */
	public AStrategy(final IStrategy<T, U> nextStrategy) {
		this.nextStrategy = nextStrategy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean preProcess(T t, U u) {
		if (nextStrategy != null) {
			return nextStrategy.preProcess(t, u);
		}
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean postProcess(T t, U u) {
		if (nextStrategy != null) {
			return nextStrategy.postProcess(t, u);
		}
		return Boolean.TRUE;
	}

}