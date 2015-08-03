package ikube.experimental;

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
        closeSearcher();
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
        IndexReader indexReader = new MultiReader(subReaders.toArray(new IndexReader[subReaders.size()]), Boolean.TRUE);
        indexSearcher = new IndexSearcher(indexReader);
    }

    synchronized void closeSearcher() throws IOException {
        if (indexSearcher != null && indexSearcher.getIndexReader() != null) {
            indexSearcher.getIndexReader().close();
        }
    }

}