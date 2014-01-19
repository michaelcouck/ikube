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
            ThreadUtilities.submit(this.getClass().getSimpleName(), new Runnable() {
                public void run() {
                    try {
                        ThreadUtilities.sleep(300000);
                        indexSearcher.getIndexReader().close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        return Boolean.TRUE;
    }

}