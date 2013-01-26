package ikube.index.handler.strategy;

import ikube.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO Comments...
 * 
 * @author Michael Couck
 * @since 12.12.12
 * @version 01.00
 */
public abstract class AStrategy implements IStrategy {

	Logger logger;

	/** The next strategy in the chain. */
	IStrategy nextStrategy;

	/**
	 * Constructor takes the next strategy, could be null.
	 * 
	 * @param next the chained strategy to execute
	 */
	public AStrategy(final IStrategy nextStrategy) {
		this.nextStrategy = nextStrategy;
		this.logger = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document,
			final Object resource) throws Exception {
		if (nextStrategy != null) {
			return nextStrategy.aroundProcess(indexContext, indexable, document, resource);
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