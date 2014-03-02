package ikube.action.rule;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.search.IndexSearcher;

import java.io.File;

/**
 * This class checks whether the index that is open is the latest index, i.e. whether there is a new index that should be opened.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 11-06-2011
 */
public class IsNewIndexCreated extends ARule<IndexContext<?>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean evaluate(final IndexContext<?> indexContext) {
        IndexSearcher indexSearcher = indexContext.getMultiSearcher();
        if (indexSearcher != null) {
            String baseIndexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
            File latestIndexDirectory = IndexManager.getLatestIndexDirectory(baseIndexDirectoryPath);
            logger.debug("Latest index directory : {} ", latestIndexDirectory);
            IndexReaderContext indexReaderContext = indexSearcher.getTopReaderContext();
            if (indexReaderContext != null && latestIndexDirectory != null) {
                int readers = indexSearcher.getTopReaderContext().children().size();
                File[] latestIndexDirectories = latestIndexDirectory.listFiles();
                if (latestIndexDirectories != null) {
                    int directories = latestIndexDirectories.length;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Readers : ", readers + ", directories : " + directories + ", directory : " + latestIndexDirectory);
                    }
                    return indexReaderContext.children().size() != latestIndexDirectories.length;
                }
            }
        }
        return new AreIndexesCreated().evaluate(indexContext);
    }

}