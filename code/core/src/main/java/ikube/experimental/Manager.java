package ikube.experimental;

import com.jcraft.jsch.JSchException;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.cluster.gg.ClusterManagerGridGain;
import ikube.cluster.listener.IListener;
import ikube.search.Search;
import ikube.search.SearchComplex;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
// @EnableAsync
@Configuration
@EnableScheduling
@SuppressWarnings("SpringJavaAutowiringInspection")
public class Manager {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Writer writer;
    private Database database;
    private Searcher searcher;

    @Autowired
    @Qualifier("ikube.cluster.gg.ClusterManagerGridGain")
    private ClusterManagerGridGain clusterManager;

    public Manager() throws IOException {
        writer = new Writer();
        database = new Database();
        searcher = new Searcher();
    }

    public void addTopicListener() {
        clusterManager.addTopicListener(IConstants.IKUBE, new IListener<Object>() {
            @Override
            public void onMessage(final Object document) {
                try {
                    writeToIndex((Document) document);
                } catch (final IOException e) {
                    logger.error("Exception writing document from grid to index : ", e);
                }
            }
        });
    }

    void writeToIndex(final Document document) throws IOException {
        writer.writeToIndex(document);
    }

    @Scheduled(initialDelay = 60000, fixedRate = 15000)
    void openSearcher() throws IOException {
        searcher.openSearcher(writer.getDirectories());
    }

    @Scheduled(initialDelay = 60000, fixedRate = 15000)
    void indexRecords() throws SQLException, JSchException {
        // Go to the database and get the changed records
        List<List<Object>> changedRecords = database.readChangedRecords();
        // Create the Lucene documents from the changed records
        List<Document> documents = writer.createDocuments(changedRecords);
        // Pop the documents in the grid to be indexed by all nodes
        for (final Document document : documents) {
            clusterManager.send(IConstants.IKUBE, document);
        }
    }

    public ArrayList<HashMap<String, String>> doSearch(final String fieldName, final String queryString)
            throws ParseException, IOException {
        Search search = new SearchComplex(searcher.getSearcher(), new StandardAnalyzer(Version.LUCENE_48));
        search.setFirstResult(0);
        search.setMaxResults(10);
        search.setFragment(Boolean.TRUE);

        search.setSearchFields(fieldName);
        search.setSearchStrings(queryString);

        search.setOccurrenceFields(IConstants.SHOULD);
        search.setTypeFields(Search.TypeField.NUMERIC.name());

        return search.execute();
    }

    public void setClusterManager(final IClusterManager clusterManager) {
        this.clusterManager = (ClusterManagerGridGain) clusterManager;
    }

}