package ikube.experimental;

import ikube.toolkit.THREAD;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class maintains the open searcher.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
public class Searcher {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private IndexSearcher indexSearcher;

    synchronized IndexSearcher getSearcher() {
        return indexSearcher;
    }

    synchronized void openSearcher(final Directory... directories) throws IOException {
        IndexReader oldIndexReader = null;
        if (indexSearcher != null) {
            oldIndexReader = indexSearcher.getIndexReader();
        }
        List<IndexReader> subReaders = new ArrayList<>();
        for (final Directory directory : directories) {
            try {
                if (!DirectoryReader.indexExists(directory)) {
                    continue;
                }
                IndexReader indexReader = DirectoryReader.open(directory);
                subReaders.add(indexReader);
            } catch (final IOException e) {
                logger.error("Exception writing to index : ", e);
            }
        }
        IndexReader[] indexReaders = subReaders.toArray(new IndexReader[subReaders.size()]);
        IndexReader newIndexReader = new MultiReader(indexReaders, Boolean.TRUE);
        indexSearcher = new IndexSearcher(newIndexReader);
        closeSearcher(oldIndexReader);
    }

    synchronized void closeSearcher(final IndexReader indexReader) {
        if (indexReader != null) {
            final String name = "index-reader-closer";
            THREAD.submit(name, new Runnable() {
                public void run() {
                    try {
                        THREAD.sleep(5000);
                        indexReader.close();
                    } catch (final IOException e) {
                        logger.error("Exception closing the old index reader, could cause file handle leak : ", e);
                    } finally {
                        THREAD.destroy(name);
                    }
                }
            });
        }
    }

}