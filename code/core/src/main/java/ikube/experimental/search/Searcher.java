package ikube.experimental.search;

import ikube.IConstants;
import ikube.experimental.listener.IConsumer;
import ikube.experimental.listener.OpenSearcherEvent;
import ikube.search.Search;
import ikube.search.SearchComplex;
import ikube.toolkit.STRING;
import ikube.toolkit.THREAD;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This class maintains the open searcher.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
@Component
public class Searcher implements IConsumer<OpenSearcherEvent> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private IndexSearcher indexSearcher;

    @Override
    public void notify(final OpenSearcherEvent event) {
        Directory[] directories = event.getData();
        logger.debug("Opening searcher : {}", Arrays.toString(directories));
        openSearcher(directories);
        logger.debug("Number of documents in searcher : {}", indexSearcher.getIndexReader().numDocs());
    }

    public ArrayList<HashMap<String, String>> doSearch(
            final String fieldName,
            final String queryString) {
        // logger.debug("Doing search : field name : " + fieldName + ", query string : "  + queryString);
        Search search = new SearchComplex(indexSearcher, new StandardAnalyzer(Version.LUCENE_48));
        search.setFirstResult(0);
        search.setMaxResults(10);
        search.setFragment(Boolean.TRUE);

        search.setSearchFields(fieldName);
        search.setSearchStrings(queryString);

        search.setOccurrenceFields(IConstants.SHOULD);
        if (STRING.isNumeric(queryString)) {
            search.setTypeFields(Search.TypeField.NUMERIC.name());
        } else {
            search.setTypeFields(Search.TypeField.STRING.name());
        }

        search.setSpellCheck(Boolean.FALSE);

        return search.execute();
    }

    void openSearcher(final Directory... directories) {
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

    void closeSearcher(final IndexReader indexReader) {
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