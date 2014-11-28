package ikube.action;

import ikube.model.IndexContext;
import ikube.toolkit.THREAD;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

/**
 * This class takes the searcher and tries to close the searcher on the directory.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 24-08-2008
 */
public class Close extends Action<IndexContext, Boolean> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean internalExecute(final IndexContext indexContext) {
        final IndexSearcher indexSearcher = indexContext.getMultiSearcher();
        if (indexSearcher != null && indexSearcher.getIndexReader() != null) {
            final String name = Long.toHexString(System.currentTimeMillis());
            THREAD.submit(name, new Runnable() {
                public void run() {
                    try {
                        THREAD.sleep(10000);
                        logger.info("Closing searcher : " + indexContext.getName() + ", " + indexSearcher.hashCode());
                        indexSearcher.getIndexReader().close();
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        THREAD.destroy(name);
                    }
                }
            });
        }
        return Boolean.TRUE;
    }

}