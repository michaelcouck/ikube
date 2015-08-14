package ikube.experimental;

import com.jcraft.jsch.JSchException;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.cluster.gg.ClusterManagerGridGain;
import ikube.cluster.listener.IListener;
import ikube.search.Search;
import ikube.search.SearchComplex;
import ikube.toolkit.STRING;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * This class pops the {@link org.apache.lucene.document.Document} in the grid, and gets any documents
 * from the grid that were popped there by another member and makes them available to the local logic that
 * will potentially add them to the index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
@Service
@Component
@EnableAsync
@Configuration
@EnableScheduling
@SuppressWarnings("SpringJavaAutowiringInspection")
public class Manager {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Writer writer;
    private Database database;
    private Searcher searcher;

    @Autowired(required = false)
    @Qualifier("ikube.cluster.gg.ClusterManagerGridGain")
    private ClusterManagerGridGain clusterManager;

    private boolean working = Boolean.FALSE;

    public Manager() throws IOException {
        writer = new Writer();
        database = new Database();
        searcher = new Searcher();

        logger.info("Manager : " + this);
    }

    public void addTopicListener() {
        clusterManager.addTopicListener(IConstants.IKUBE, new IListener<Object>() {
            @Override
            public void onMessage(final Object document) {
                try {
                    // logger.info("Got message : " + document);
                    writeToIndex((Document) document);
                } catch (final IOException e) {
                    logger.error("Exception writing document from grid to index : ", e);
                }
            }
        });
        logger.info("Added topic listener : ");
    }

    /* synchronized */ void writeToIndex(final Document document) throws IOException {
        logger.debug("Writing to index : {}", document);
        writer.writeToIndex(document);
    }

    @Scheduled(initialDelay = 10000, fixedRate = 5000)
    /* synchronized */ void openSearcher() throws IOException {
        logger.debug("Opening searcher : {}", Arrays.toString(writer.getDirectories()));
        searcher.openSearcher(writer.getDirectories());
        logger.debug("Number of documents in searcher : {}", searcher.getSearcher().getIndexReader().numDocs());
    }

    @Scheduled(initialDelay = 10000, fixedRate = 5000)
    void indexRecords() throws SQLException, JSchException {
        synchronized (this) {
            try {
                if (working) {
                    return;
                }
                working = Boolean.TRUE;
            } finally {
                notifyAll();
            }
        }
        try {
            // Go to the database and get the changed records
            List<Map<Object, Object>> changedRecords = database.readChangedRecords();
            // Create the Lucene documents from the changed records
            List<Document> documents = writer.createDocuments(changedRecords);
            if (documents.size() > 0) {
                logger.info("Documents to add : " + documents.size());
                // Pop the documents in the grid to be indexed by all nodes
                for (final Document document : documents) {
                    logger.debug("Sending document to grid : ");
                    clusterManager.send(IConstants.IKUBE, document);
                }
            }
        } finally {
            working = Boolean.FALSE;
        }
    }

    public ArrayList<HashMap<String, String>> doSearch(
            final String fieldName,
            final String queryString) {
        // logger.debug("Doing search : field name : " + fieldName + ", query string : "  + queryString);
        Search search = new SearchComplex(searcher.getSearcher(), new StandardAnalyzer(Version.LUCENE_48));
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

    public void setClusterManager(final IClusterManager clusterManager) {
        this.clusterManager = (ClusterManagerGridGain) clusterManager;
    }

    protected void printIndex(final int numDocs) {
        IndexReader indexReader = searcher.getSearcher().getIndexReader();
        logger.error("Num docs : " + indexReader.numDocs());
        for (int i = 0; i < numDocs && i < indexReader.numDocs(); i++) {
            try {
                Document document = indexReader.document(i);
                logger.error("Document : " + i + ", " + document.toString().length());
                printDocument(document);
            } catch (IOException e) {
                logger.error(null, e);
            }
        }
    }

    protected void printDocument(final Document document) {
        List<IndexableField> fields = document.getFields();
        for (IndexableField indexableField : fields) {
            logger.error("        : {}", indexableField);
        }
    }

}