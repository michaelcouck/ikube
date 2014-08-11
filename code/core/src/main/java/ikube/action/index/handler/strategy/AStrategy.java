package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategies are a plugin point for processing the data at the point where the resource is retrieved and the
 * content is available. For example once the table row has been read, and the data is available in the column objects,
 * the geospatial strategy will then check if there are co-ordinates for the row, and if so then enrich the index
 * with a geo-hash. If the co-ordinates are not available then the data in the columns is used to try and find the
 * address in the geocoder. This principal can be used for analytical data processing and so on.
 * <p/>
 * Note that if the strategy returns false the processing stops abruptly for this resource. If the strategy logic
 * will allow processing and there is a next strategy in the chain, then the program flow is passed to the next strategy
 * to execute logic and indeed decide if the processing should continue for the current resource.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-12-2012
 */
@SuppressWarnings("ALL")
abstract class AStrategy implements IStrategy {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The next strategy in the chain.
     */
    IStrategy nextStrategy;

    /**
     * Constructor takes the next strategy, could be null.
     *
     * @param nextStrategy the chained strategy to execute
     */
    AStrategy(final IStrategy nextStrategy) {
        this.nextStrategy = nextStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean aroundProcess(final IndexContext indexContext, final Indexable indexable, final Document document, final Object resource) throws Exception {
        if (nextStrategy != null) {
            return nextStrategy.aroundProcess(indexContext, indexable, document, resource);
        }
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean postProcess(final IndexContext indexContext, final Indexable indexable, final Document document, final Object resource) throws Exception {
        // Not all strategies need to post process of course
        if (nextStrategy != null) {
            return nextStrategy.postProcess(indexContext, indexable, document, resource);
        }
        return Boolean.TRUE;
    }

    /**
     * Opportunity to init anything in the strategies.
     */
    public void initialize() {
        // To be over ridden by sub classes
    }

    public void setNextStrategy(IStrategy nextStrategy) {
        this.nextStrategy = nextStrategy;
    }

}