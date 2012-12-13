package ikube.index.handler.strategy;

import ikube.index.handler.IStrategy;

/**
 * This is the delta strategy for the file system handler. Essentially what this class should do is to check to see if the document/file
 * being processed already exists in the current index. If it does, and the time stamp and the length are the same then return a false
 * indicator, meaning that the handler should not add this document to the index.
 * 
 * @author Michael Couck
 * @since 12.12.12
 * @version 01.00
 */
public abstract class AStrategy<T, U> implements IStrategy<T, U> {

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

}
