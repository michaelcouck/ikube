package ikube.action.rule;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.CompositeReaderContext;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.MMapDirectory;

import java.io.File;
import java.util.List;

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
    @SuppressWarnings("LoopStatementThatDoesntLoop")
    public boolean evaluate(final IndexContext<?> indexContext) {
        IndexSearcher indexSearcher = indexContext.getMultiSearcher();
        if (indexSearcher != null) {
            String baseIndexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
            File latestIndexDirectory = IndexManager.getLatestIndexDirectory(baseIndexDirectoryPath);
            logger.debug("Latest index directory : {} ", latestIndexDirectory);
            MultiReader multiReader = (MultiReader) indexSearcher.getIndexReader();
            CompositeReaderContext compositeReaderContext = multiReader.getContext();
            List<AtomicReaderContext> atomicReaderContexts = compositeReaderContext.leaves();
            for (final AtomicReaderContext atomicReaderContext : atomicReaderContexts) {
                SegmentReader atomicReader = (SegmentReader) atomicReaderContext.reader();
                logger.debug("Atomic reader : " + atomicReader.getClass().getName());
                MMapDirectory directory = (MMapDirectory) atomicReader.directory();
                logger.debug("Directory : " + directory.getClass().getName());
                File indexDirectory = directory.getDirectory();
                File parentIndexDirectory = indexDirectory.getParentFile();
                logger.debug("Parent : " + parentIndexDirectory + ", " + latestIndexDirectory);
                if (!latestIndexDirectory.equals(parentIndexDirectory)) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            }
        }
        return new AreIndexesCreated().evaluate(indexContext);
    }

}