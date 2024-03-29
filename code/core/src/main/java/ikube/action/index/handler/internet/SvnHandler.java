package ikube.action.index.handler.internet;

import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableSvn;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * This class will create the SVN provider and the handler, and feed them into the
 * threading mechanism, specifically the recursive action that implements the fork/join
 * logic to spawn threads for indexing.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 04-06-2014
 */
public class SvnHandler extends IndexableHandler<IndexableSvn> {

    @Autowired
    private SvnResourceHandler svnResourceHandler;

    /**
     * {@inheritDoc}
     */
    @Override
    public ForkJoinTask<?> handleIndexableForked(final IndexContext indexContext, final IndexableSvn indexable) throws Exception {
        SvnResourceProvider svnResourceProvider = new SvnResourceProvider(indexable);
        return getRecursiveAction(indexContext, indexable, svnResourceProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<?> handleResource(final IndexContext indexContext, final IndexableSvn indexable, final Object resource) {
        try {
            svnResourceHandler.handleResource(indexContext, indexable, new Document(), resource);
        } catch (final Exception e) {
            handleException(indexable, e, "Exception handling svn resource : " + resource);
        }
        return null;
    }
}