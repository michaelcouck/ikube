package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategies are a plugin point for processing the data at the point where the resource is retrieved and the content is available. For example once the table
 * row has been read, and the data is available in the column objects, the geospatial strategy will then check if there are co-ordinates for the row, and if so
 * then enrich the index with a geo-hash. If the co-ordinates are not available then the data in the columns is used to try and find the address in the
 * geocoder. This principal can be used for analytical data processing and so on.
 * 
 * Note that if the strategy returns false the processing stops abruptly for this resource. If the strategy logic will allow processing and there is a next
 * strategy in the chain, then the program flow is passed to the next strategy to execute logic and indeed decide if the processing should continue for the
 * current resource.
 * 
 * @author Michael Couck
 * @since 12.12.12
 * @version 01.00
 */
public abstract class AStrategy implements IStrategy {

	Logger logger = LoggerFactory.getLogger(this.getClass());

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
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document, final Object resource)
			throws Exception {
		if (nextStrategy != null) {
			return nextStrategy.aroundProcess(indexContext, indexable, document, resource);
		}
		return Boolean.TRUE;
	}

	/**
	 * This method walks backward up the composite hierarchy until it gets to the context, which should be the top level indexable, i.e. the grand parent of all
	 * indexables.
	 * 
	 * @param indexable the indexable to start looking for the index context from
	 * @return the index context grand parent of the indexable
	 */
	protected IndexContext<?> getIndexContext(final Indexable<?> indexable) {
		if (IndexContext.class.isAssignableFrom(indexable.getClass())) {
			return (IndexContext<?>) indexable;
		}
		return getIndexContext(indexable.getParent());
	}

	/**
	 * Opportunity to init anything in the strategies.
	 */
	public void initialize() {
		// To be over ridden by sub classes
	}

}