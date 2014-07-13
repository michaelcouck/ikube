package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.IndexableExchange;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * @author David Turley
 * @version 01.00
 * @since 11-07-2014
 */
@SuppressWarnings({"SpringJavaAutowiredMembersInspection", "SpringJavaAutowiringInspection"})
public class ExchangeHandler extends IndexableHandler<IndexableExchange> {

    /**
     * This is a generic dao that can be used for persisting things to the database if necessary.
     */
    @Autowired
    private IDataBase dataBase;
    /**
     * This bean will crawl the Exchange server and provide resources to this handler, one at a time.
     */
    @Autowired
    private ExchangeResourceHandler exchangeResourceHandler;

    /**
     * TODO: This starts the threads working on the target source of data
     */
    @Override
    public ForkJoinTask<?> handleIndexableForked(final IndexContext indexContext, final IndexableExchange indexableExchange) throws Exception {
        IResourceProvider resourceProvider = new ExchangeResourceProvider(indexableExchange);
        return getRecursiveAction(indexContext, indexableExchange, resourceProvider);
    }

    /**
     * TODO: This handles one resource at a time, called by the super class after acquiring the
     * resource from the assigned {@link ikube.action.index.handler.IResourceProvider} for this class.
     */
    @Override
    protected List<?> handleResource(final IndexContext indexContext, final IndexableExchange indexableExchange, final Object resource) {
        try {
            exchangeResourceHandler.handleResource(indexContext, indexableExchange, new Document(), resource);
        } catch (final Exception e) {
            handleException(indexableExchange, e, "Exception handling email : ");
        }
        return Collections.EMPTY_LIST;
    }
}