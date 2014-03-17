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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This rule checks whether there are indexes that are created but are not yet opened. This typically
 * needs to be checked if an index is still in the process of being generated. In this case when the index
 * is finished being created the searcher should be opened on all the index directories.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-02-2011
 */
public class AreUnopenedIndexes extends ARule<IndexContext<?>> {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean evaluate(final IndexContext<?> indexContext) {
        IndexSearcher indexSearcher = indexContext.getMultiSearcher();
        if (indexSearcher == null) {
            return new AreIndexesCreated().evaluate(indexContext);
        }
        String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
        if (latestIndexDirectory == null ||
                latestIndexDirectory.listFiles() == null ||
                latestIndexDirectory.listFiles().length == 0) {
            return Boolean.FALSE;
        }
        // This block checks that all the directories that have
        // indexes are in fact opened and are accessible in the index readers
        // of the index searcher
        logger.debug("Checking latest index directory for new indexes : {} ", latestIndexDirectory);
        List<File> indexDirectories = new ArrayList<>(Arrays.asList(latestIndexDirectory.listFiles()));
        MultiReader multiReader = (MultiReader) indexSearcher.getIndexReader();
        CompositeReaderContext compositeReaderContext = multiReader.getContext();
        List<AtomicReaderContext> atomicReaderContexts = compositeReaderContext.leaves();
        for (final AtomicReaderContext atomicReaderContext : atomicReaderContexts) {
            SegmentReader atomicReader = (SegmentReader) atomicReaderContext.reader();
            MMapDirectory directory = (MMapDirectory) atomicReader.directory();
            logger.info("Directory : " + directory.getDirectory());
            for (int i = 0; i < indexDirectories.size(); i++) {
                File indexDirectory = indexDirectories.get(i);
                File openedIndexDirectory = directory.getDirectory();
                String l = FileUtilities.cleanFilePath(indexDirectory.getAbsolutePath());
                String p = FileUtilities.cleanFilePath(openedIndexDirectory.getAbsolutePath());
                boolean isOpened = l.equals(p);
                logger.info("        : " + isOpened + ", " + indexDirectory);
                if (isOpened) {
                    indexDirectories.remove(i);
                }
            }
        }
        logger.info("Index directories left : " + indexDirectories);
        // return atomicReaderContexts.size() != latestIndexDirectory.listFiles().length;
        return indexDirectories.size() > 0;
    }

}
