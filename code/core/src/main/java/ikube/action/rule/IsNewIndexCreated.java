package ikube.action.rule;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.StringUtilities;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.CompositeReaderContext;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.MMapDirectory;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * This class checks whether the index that is open is the latest index, i.e. whether there is a new index that should be opened.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 11-06-2011
 */
public class IsNewIndexCreated extends ARule<IndexContext> {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("LoopStatementThatDoesntLoop")
    public boolean evaluate(final IndexContext indexContext) {
        IndexSearcher indexSearcher = indexContext.getMultiSearcher();
        if (indexSearcher == null) {
            String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
            File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
            return latestIndexDirectory != null;
        }
        Date current = null;
        Date latest = IndexManager.getLatestIndexDirectoryDate(indexContext);
        String baseIndexDirectory = IndexManager.getIndexDirectoryPath(indexContext);
        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(baseIndexDirectory);

        MultiReader multiReader = (MultiReader) indexSearcher.getIndexReader();
        CompositeReaderContext compositeReaderContext = multiReader.getContext();
        List<AtomicReaderContext> atomicReaderContexts = compositeReaderContext.leaves();
        printReaders(atomicReaderContexts);

        File openedIndexDirectory = null;
        for (final AtomicReaderContext atomicReaderContext : atomicReaderContexts) {
            SegmentReader atomicReader = (SegmentReader) atomicReaderContext.reader();
            MMapDirectory directory = (MMapDirectory) atomicReader.directory();
            openedIndexDirectory = directory.getDirectory();
            do {
                if (StringUtilities.isNumeric(openedIndexDirectory.getName())) {
                    current = new Date(Long.parseLong(openedIndexDirectory.getName()));
                    break;
                }
                openedIndexDirectory = openedIndexDirectory.getParentFile();
            } while (openedIndexDirectory.getParentFile() != null);
            break;
        }
        if (current == null) {
            logger.info("Not really open then : " + indexContext.getName());
            return Boolean.FALSE;
        }

        logger.info("Opened : " + openedIndexDirectory);
        logger.info("Latest : " + latestIndexDirectory);

        boolean isNewIndexCreated = !latest.equals(current);
        logger.info("Index created : " + isNewIndexCreated +
                "," + indexContext.getName() +
                ", " + latest.getTime() +
                ", " + current.getTime());
        return isNewIndexCreated;
    }

    private void printReaders(final List<AtomicReaderContext> atomicReaderContexts) {
        for (final AtomicReaderContext atomicReaderContext : atomicReaderContexts) {
            SegmentReader atomicReader = (SegmentReader) atomicReaderContext.reader();
            MMapDirectory directory = (MMapDirectory) atomicReader.directory();
            File openedIndexDirectory = directory.getDirectory();
            logger.info("Opened index directory : " + openedIndexDirectory);
        }
    }

}