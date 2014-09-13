package ikube.action;

import ikube.model.IndexContext;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;

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
public class Reopen extends Open {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean internalExecute(final IndexContext indexContext) {
        try {
            openNewReaders(indexContext);
            return Boolean.TRUE;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    // NOTE: This has a memory leak n the FST class *********************
    // NOTE: 13-09-2014: It seems that in the 4.8 version of Lucene this is fixed!! Yahoo!!!

    @SuppressWarnings("UnusedDeclaration")
    void openNewReaders(final IndexContext indexContext) throws Exception {
        IndexSearcher oldIndexSearcher = indexContext.getMultiSearcher();

        if (oldIndexSearcher == null) {
            openOnFile(indexContext);
            return;
        }

        List<IndexReader> newIndexReaders = new ArrayList<>();
        List<IndexReader> oldIndexReaders = new ArrayList<>();
        MultiReader multiReader = (MultiReader) oldIndexSearcher.getIndexReader();
        CompositeReaderContext compositeReaderContext = multiReader.getContext();
        for (final IndexReaderContext indexReaderContext : compositeReaderContext.children()) {
            IndexReader oldIndexReader = indexReaderContext.reader();
            IndexReader newIndexReader = DirectoryReader.openIfChanged((DirectoryReader) oldIndexReader);
            if (newIndexReader != null && oldIndexReader != newIndexReader) {
                logger.info("New index reader : " + newIndexReader.hashCode());
                newIndexReaders.add(newIndexReader);
                oldIndexReaders.add(oldIndexReader);
            } else {
                if (oldIndexReader != null) {
                    newIndexReaders.add(oldIndexReader);
                    logger.info("Keeping old index reader : " + oldIndexReader.hashCode());
                }
            }
        }
        int newIndexReadersSize = newIndexReaders.size();
        IndexReader[] newIndexReaderArray = newIndexReaders.toArray(new IndexReader[newIndexReadersSize]);
        IndexReader indexReader = new MultiReader(newIndexReaderArray, Boolean.FALSE);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        indexContext.setMultiSearcher(indexSearcher);
        for (final IndexReader oldIndexReader : oldIndexReaders) {
            try {
                if (oldIndexReader != null) {
                    oldIndexReader.close();
                }
            } catch (final Exception e) {
                logger.error("Exception closing the old index reader : " + oldIndexReader, e);
            }
        }
        logger.info("Opened new searcher : " + indexContext.getMultiSearcher().getIndexReader().numDocs());
    }

    // NOTE: 13-09-2014: This finally runs out of memory, open file handles increase gradually

    //    void openOnIndexWriters(final IndexContext indexContext) throws Exception {
//        List<IndexReader> newIndexReaders = new ArrayList<>();
//        IndexSearcher oldIndexSearcher = indexContext.getMultiSearcher();
//        if (indexContext.getIndexWriters() == null) {
//            logger.info("Index writers not initialized for delta index : " + indexContext.getName());
//            openOnFile(indexContext);
//        } else {
//            for (final IndexWriter indexWriter : indexContext.getIndexWriters()) {
//                Directory directory = indexWriter.getDirectory();
//                if (!DirectoryReader.indexExists(directory)) {
//                    logger.warn("Directory for writer does not exist : " + directory);
//                    continue;
//                }
//                IndexReader newIndexReader = DirectoryReader.open(directory);
//                newIndexReaders.add(newIndexReader);
//            }
//            int newIndexReadersSize = newIndexReaders.size();
//            if (newIndexReadersSize > 0) {
//                logger.info("Reopening index : " + indexContext.getName());
//                IndexReader[] newIndexReaderArray = new IndexReader[newIndexReadersSize];
//                newIndexReaderArray = newIndexReaders.toArray(newIndexReaderArray);
//                IndexReader indexReader = new MultiReader(newIndexReaderArray, Boolean.FALSE);
//                IndexSearcher indexSearcher = new IndexSearcher(indexReader);
//                indexContext.setMultiSearcher(indexSearcher);
//
//                if (oldIndexSearcher != null) {
//                    logger.info("Closing old delta index reader : " + indexContext.getName());
//                    close(oldIndexSearcher.getIndexReader());
//                }
//            }
//        }
//    }

    // NOTE: This has a memory leak too, somewhere... ********************

    /*void open(final IndexContext<?> indexContext) throws Exception {
        new Open().execute(indexContext);
    }*/

}