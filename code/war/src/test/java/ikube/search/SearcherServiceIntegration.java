package ikube.search;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.cluster.IClusterManager;
import ikube.model.Search;
import ikube.toolkit.THREAD;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashMap;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static mockit.Deencapsulation.invoke;

/**
 * TODO: This test fails because Hazelcast is not ready with the map! Jesus, and
 * the search needs to be persisted, so the test fails. WTF! So when GridGain replaces
 * Hazelcast then this test can run again.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 01-06-2014
 */
@Ignore
@SuppressWarnings("SpringJavaAutowiringInspection")
public class SearcherServiceIntegration extends IntegrationTest {

    @Autowired
    @Qualifier(value = "ikube.cluster.IClusterManager")
    private IClusterManager clusterManager;
    @Autowired
    private ISearcherService searcherService;

    @Test
    public void persistSearch() {
        Search search = new Search();

        ArrayList<HashMap<String, String>> results = new ArrayList<>();
        HashMap<String, String> result = new HashMap<>();
        HashMap<String, String> statistics = new HashMap<>();

        statistics.put(IConstants.TOTAL, "100");
        statistics.put(IConstants.DURATION, "100");
        statistics.put(IConstants.SCORE, "100");
        statistics.put(IConstants.SEARCH_STRINGS, "searchString");
        statistics.put(IConstants.CORRECTIONS, "correctedSearchString");

        results.add(result);
        results.add(statistics);

        search.setIndexName(IConstants.GEOSPATIAL);
        search.setSearchStrings(asList(IConstants.SEARCH_STRINGS));
        search.setSearchResults(results);

        invoke(searcherService, "persistSearch", search);

        int iterations = 3;
        for (int i = 0; i < iterations; i++) {
            invoke(searcherService, "persistSearch", search);
        }
        THREAD.sleep(15000);

        Search dbSearch = clusterManager.get(IConstants.SEARCH, search.getHash());
        assertNotNull(dbSearch);
        assertTrue(iterations <= dbSearch.getCount());
    }

}