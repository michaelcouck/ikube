package ikube.search;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.database.IDataBase;
import ikube.model.Search;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ThreadUtilities;
import junit.framework.Assert;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 01-06-2014
 */
public class SearcherServiceIntegration extends IntegrationTest {

    private ISearcherService searcherService;

    @Before
    public void before() {
        Map<String, ISearcherService> beans = ApplicationContextManager.getBeans(ISearcherService.class);
        searcherService = beans.values().iterator().next();
    }

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
        ThreadUtilities.sleep(20000);

        IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
        Search dbSearch = dataBase.find(Search.class, search.getId());
        logger.info("Count : " + dbSearch.getCount() + ":" + iterations);
        Assert.assertNotNull(dbSearch);
        Assert.assertTrue(iterations < dbSearch.getCount());
    }

}