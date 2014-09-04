package ikube.search;

import ikube.IConstants;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * This class will open the index with a searcher and execute a query on the index for the
 * specified field and string, and print the results to the output. Note that the analyzer is
 * the {@link StandardAnalyzer} as the specific analyzers are in the core and this module
 * does not have the core as a dependency.
 *
 * @author Michael Couck
 * @version 01.00
 * @since at least 14-04-2012
 */
public final class SearchToolkit {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchToolkit.class);

    public static void main(final String[] args) {
        File indexDirectory = new File(args[0]);
        String searchField = args[1];
        String searchString = args[2];
        IndexSearcher indexSearcher = null;
        try {
            Directory directory = NIOFSDirectory.open(indexDirectory);
            IndexReader indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
            Query query = new QueryParser(IConstants.LUCENE_VERSION, searchField, new StandardAnalyzer(IConstants.LUCENE_VERSION)).parse(searchString);
            TopDocs topDocs = indexSearcher.search(query, 100);
            long totalHits = topDocs.totalHits;
            long scoreHits = topDocs.scoreDocs.length;
            for (int i = 0; i < totalHits && i < scoreHits; i++) {
                Document document = indexSearcher.doc(topDocs.scoreDocs[i].doc);
                LOGGER.error("Document : " + i + ", " + document);
            }
        } catch (final Exception e) {
            usage();
            LOGGER.error("Exception printing index : " + Arrays.deepToString(args), e);
        } finally {
            if (indexSearcher != null) {
                try {
                    indexSearcher.getIndexReader().close();
                } catch (final IOException e) {
                    LOGGER.error("Exception closing the index searcher : ", e);
                }
            }
        }
    }

    private static void usage() {
        LOGGER.info("Usage: java -jar ikube-tools.jar ikube.search.SearchToolkit /tmp contents world");
        LOGGER.info("Usage: java -jar ikube-tools.jar ikube.search.SearchToolkit index-directory field-in-index search-string");
    }

}
