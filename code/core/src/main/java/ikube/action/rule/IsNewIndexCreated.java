package ikube.action.rule;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.StringUtilities;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.CompositeReaderContext;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.MMapDirectory;

import java.io.File;
import java.util.Date;
import java.util.List;

import static ikube.action.index.IndexManager.getIndexDirectoryPath;
import static ikube.action.index.IndexManager.getLatestIndexDirectory;

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
            String indexDirectoryPath = getIndexDirectoryPath(indexContext);
            File latestIndexDirectory = getLatestIndexDirectory(indexDirectoryPath);
            return latestIndexDirectory != null;
        }
        Date current = null;
        Date latest = IndexManager.getLatestIndexDirectoryDate(indexContext);
        String baseIndexDirectory = getIndexDirectoryPath(indexContext);
        File latestIndexDirectory = getLatestIndexDirectory(baseIndexDirectory);

        MultiReader multiReader = (MultiReader) indexSearcher.getIndexReader();

        List<AtomicReaderContext> atomicReaderContexts;
        try {
            CompositeReaderContext compositeReaderContext = multiReader.getContext();
            atomicReaderContexts = compositeReaderContext.leaves();
        } catch (final AlreadyClosedException e) {
            logger.error("This index is closed : " + indexContext.getName());
            logger.debug(null, e);
            return Boolean.FALSE;
        }

        File openedIndexDirectory = null;
        for (final AtomicReaderContext atomicReaderContext : atomicReaderContexts) {
            SegmentReader atomicReader = (SegmentReader) atomicReaderContext.reader();
            MMapDirectory directory = (MMapDirectory) atomicReader.directory();
            try {
                openedIndexDirectory = directory.getDirectory();
                do {
                    if (StringUtilities.isNumeric(openedIndexDirectory.getName())) {
                        current = new Date(Long.parseLong(openedIndexDirectory.getName()));
                        break;
                    }
                    openedIndexDirectory = openedIndexDirectory.getParentFile();
                } while (openedIndexDirectory != null);
            } catch (final Exception e) {
                logger.debug("Is this index closed already? : " + indexContext.getName(), e);
            }
            break;
        }
        if (current == null) {
            logger.debug("Not really open then : " + indexContext.getName());
            return Boolean.FALSE;
        }

        logger.debug("Opened : " + openedIndexDirectory);
        logger.debug("Latest : " + latestIndexDirectory);

        boolean isNewIndexCreated = !latest.equals(current);
        logger.debug("Index created : " + isNewIndexCreated +
                "," + indexContext.getName() +
                ", " + latest.getTime() +
                ", " + current.getTime());
        return isNewIndexCreated;
    }

}