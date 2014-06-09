package ikube.search;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.database.IDataBase;
import ikube.model.Search;
import ikube.toolkit.ThreadUtilities;
import junit.framework.Assert;
import mockit.Deencapsulation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 01-06-2014
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class SearcherServiceIntegration extends IntegrationTest {

    @Autowired
    private IDataBase dataBase;
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
        search.setSearchStrings(Arrays.asList(IConstants.SEARCH_STRINGS));
        search.setSearchResults(results);

        Deencapsulation.invoke(searcherService, "persistSearch", search);

        int iterations = 3;
        for (int i = 0; i < iterations; i++) {
            Deencapsulation.invoke(searcherService, "persistSearch", search);
        }
        ThreadUtilities.sleep(15000);

        Search dbSearch = dataBase.find(Search.class, search.getId());
        logger.info("Search count : " + dbSearch + ", iterations : " + iterations);
        Assert.assertNotNull(dbSearch);
        Assert.assertTrue(iterations <= dbSearch.getCount());
    }

}