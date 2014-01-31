package ikube.action;

import ikube.model.IndexContext;
import ikube.toolkit.ThreadUtilities;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

/**
 * This class takes the searcher and tries to close the searcher on the directory.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 24.08.08
 */
public class Close extends Action<IndexContext<?>, Boolean> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean internalExecute(final IndexContext<?> indexContext) {
        final IndexSearcher indexSearcher = indexContext.getMultiSearcher();
        if (indexSearcher != null && indexSearcher.getIndexReader() != null) {
            final String name = Long.toHexString(System.currentTimeMillis());
            ThreadUtilities.submit(name, new Runnable() {
                public void run() {
                    try {
                        ThreadUtilities.sleep(60000);
                        logger.info("Closing searcher : " + indexContext.getName() + ", " + indexSearcher.hashCode());
                        indexSearcher.getIndexReader().close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        ThreadUtilities.destroy(name);
                    }
                }
            });
        }
        return Boolean.TRUE;
    }

}