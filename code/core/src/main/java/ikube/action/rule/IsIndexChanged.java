package ikube.action.rule;

import ikube.model.IndexContext;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20.21.2013
 */
public class IsIndexChanged extends ARule<IndexContext<?>> {

    @Override
    public boolean evaluate(final IndexContext<?> indexContext) {
        IndexSearcher oldIndexSearcher = indexContext.getMultiSearcher();
        boolean indexChanged = Boolean.FALSE;
        try {
            if (oldIndexSearcher != null && oldIndexSearcher.getIndexReader() != null) {
                MultiReader oldMultiReader = (MultiReader) oldIndexSearcher.getIndexReader();
                CompositeReaderContext compositeReaderContext = oldMultiReader.getContext();
                for (final IndexReaderContext indexReaderContext : compositeReaderContext.children()) {
                    IndexReader oldIndexReader = indexReaderContext.reader();
                    IndexReader newIndexReader = null;
                    try {
                        newIndexReader = DirectoryReader.openIfChanged((DirectoryReader) oldIndexReader);
                    } catch (Exception e) {
                        logger.error("Exception checking directory for changes : ", e);
                    } finally {
                        if (newIndexReader != null) {
                            newIndexReader.close();
                            indexChanged = Boolean.TRUE;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return indexChanged;
    }

}