package ikube.action.rule;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;
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
        String baseIndexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(baseIndexDirectoryPath);
        if (indexSearcher != null) {
            logger.info("Latest index directory : " + latestIndexDirectory);
            MultiReader multiReader = (MultiReader) indexSearcher.getIndexReader();
            CompositeReaderContext compositeReaderContext = multiReader.getContext();
            List<AtomicReaderContext> atomicReaderContexts = compositeReaderContext.leaves();
            for (final AtomicReaderContext atomicReaderContext : atomicReaderContexts) {
                SegmentReader atomicReader = (SegmentReader) atomicReaderContext.reader();
                MMapDirectory directory = (MMapDirectory) atomicReader.directory();
                File indexDirectory = directory.getDirectory();
                File parentIndexDirectory = indexDirectory.getParentFile();
                String l = FileUtilities.cleanFilePath(latestIndexDirectory.getAbsolutePath()).trim();
                String p = FileUtilities.cleanFilePath(parentIndexDirectory.getAbsolutePath()).trim();
                logger.info("Parent : " + l.equals(p) + ", " + l + ", " + p);
                boolean openOnLatest = l.equals(p);
                if (openOnLatest) {
                    logger.info("Latest : " + latestIndexDirectory);
                    return Boolean.FALSE;
                } else {
                    logger.info("Not latest : " + latestIndexDirectory);
                    return Boolean.TRUE;
                }
            }
        }
        return latestIndexDirectory != null;
    }

}