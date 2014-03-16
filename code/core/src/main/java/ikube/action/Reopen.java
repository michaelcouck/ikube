package ikube.action;

import ikube.model.IndexContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This action will re-open the indexes in the case it is a delta index. The index must be in progress, and the
 * index writers must be active as the directories are taken from the index writers to create the index readers. This
 * seems to be the only memory stable way to reopen the indexes.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 22-06-2013
 */
public class Reopen extends Action<IndexContext<?>, Boolean> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean internalExecute(final IndexContext<?> indexContext) {
        try {
            openOnIndexWriters(indexContext);
            return Boolean.TRUE;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    void openOnIndexWriters(final IndexContext<?> indexContext) throws IOException {
        if (indexContext.getIndexWriters() == null) {
            return;
        }
        IndexSearcher oldIndexSearcher = indexContext.getMultiSearcher();
        List<IndexReader> newIndexReaders = new ArrayList<>();
        for (final IndexWriter indexWriter : indexContext.getIndexWriters()) {
            Directory directory = indexWriter.getDirectory();
            if (!DirectoryReader.indexExists(directory)) {
                logger.warn("Directory for writer does not exist : " + directory);
                continue;
            }
            IndexReader newIndexReader = DirectoryReader.open(directory);
            newIndexReaders.add(newIndexReader);
        }
        int newIndexReadersSize = newIndexReaders.size();
        IndexReader[] newIndexReaderArray = new IndexReader[newIndexReadersSize];
        newIndexReaderArray = newIndexReaders.toArray(newIndexReaderArray);
        IndexReader indexReader = new MultiReader(newIndexReaderArray, Boolean.FALSE);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        indexContext.setMultiSearcher(indexSearcher);
        logger.info("Re-opening searcher : " + indexContext.getName());
        if (oldIndexSearcher != null && oldIndexSearcher.getIndexReader() != null) {
            oldIndexSearcher.getIndexReader().close();
        }
    }

    /*void open(final IndexContext<?> indexContext) throws Exception {
        // This has a memory leak too, somewhere...
        new Open().execute(indexContext);
    }*/

    /*@SuppressWarnings("UnusedDeclaration")
    void openNewReaders(final IndexContext<?> indexContext, final IndexSearcher oldIndexSearcher) throws IOException {
        // This has a memory leak n the FST class
        boolean hasNewReader = Boolean.FALSE;
        List<IndexReader> newIndexReaders = new ArrayList<>();
        MultiReader oldMultiReader = (MultiReader) oldIndexSearcher.getIndexReader();
        CompositeReaderContext oldCompositeReaderContext = oldMultiReader.getContext();
        for (final IndexReaderContext oldIndexReaderContext : oldCompositeReaderContext.children()) {
            IndexReader oldIndexReader = oldIndexReaderContext.reader();
            IndexReader newIndexReader = DirectoryReader.openIfChanged((DirectoryReader) oldIndexReader);
            if (newIndexReader != null) {
                hasNewReader = Boolean.TRUE;
                logger.info("New index reader : " + newIndexReader.hashCode());
                oldIndexReader.close();
                newIndexReaders.add(newIndexReader);
            } else {
                logger.info("Keeping old index reader : " + oldIndexReader.hashCode());
                newIndexReaders.add(oldIndexReader);
            }
        }
        if (hasNewReader) {
            int newIndexReadersSize = newIndexReaders.size();
            IndexReader[] newIndexReaderArray = newIndexReaders.toArray(new IndexReader[newIndexReadersSize]);
            IndexReader indexReader = new MultiReader(newIndexReaderArray, Boolean.FALSE);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            indexContext.setMultiSearcher(indexSearcher);
            logger.info("Opening new searcher : " + indexContext.getMultiSearcher());
        }
    }*/

}