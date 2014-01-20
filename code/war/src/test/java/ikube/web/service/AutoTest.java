package ikube.web.service;

import ikube.BaseTest;
import ikube.IConstants;
import ikube.model.Search;
import ikube.search.ISearcherService;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AutoTest extends BaseTest {

    /**
     * Class under test
     */
    private Auto auto;
    private ISearcherService searcherService;

    @Before
    public void before() throws Exception {
        auto = new Auto();
        searcherService = mock(ISearcherService.class);
        Deencapsulation.setField(auto, searcherService);
    }

    @Test
    public void auto() {
        // TODO Redo...
    }

    @Test
    public void suggestions() throws Exception {
        String string = "hello AND world";
        Search search = new Search();
        search.setMaxResults(7);

        Search helloSearch = getSearch(new Search(), 2, "hello", "helloed");
        Search worldSearch = getSearch(new Search(), 2, "world", "worldly");
        when(searcherService.search(any(Search.class))).thenReturn(helloSearch, worldSearch);
        StringBuilder[] suggestions = auto.suggestions(string, search);
        assertEquals(2, suggestions.length);

        helloSearch = getSearch(new Search(), 3, "hello", "helloed", "helloes");
        worldSearch = getSearch(new Search(), 3, "world", "worldly", "worldliness");
        when(searcherService.search(any(Search.class))).thenReturn(helloSearch, worldSearch);
        suggestions = auto.suggestions(string, search);
        assertEquals(3, suggestions.length);

        helloSearch = getSearch(new Search(), 0);
        worldSearch = getSearch(new Search(), 0);
        when(searcherService.search(any(Search.class))).thenReturn(helloSearch, worldSearch);
        suggestions = auto.suggestions(string, search);
        assertEquals(0, suggestions.length);

        helloSearch = getSearch(new Search(), 8, "hello", "helloed", "helloes");
        worldSearch = getSearch(new Search(), 8, "world", "worldly", "worldliness");
        when(searcherService.search(any(Search.class))).thenReturn(helloSearch, worldSearch);
        suggestions = auto.suggestions(string, search);
        printSuggestions(suggestions);
        assertEquals(7, suggestions.length);
    }

    private Search getSearch(final Search search, final int total, final String... fragments) {
        ArrayList<HashMap<String, String>> results = new ArrayList<>();
        for (final String fragment : fragments) {
            HashMap<String, String> result = new HashMap<>();
            result.put(IConstants.FRAGMENT, "<b>" + fragment + "</b>");
            results.add(result);
        }
        HashMap<String, String> statistics = new HashMap<>();
        statistics.put(IConstants.TOTAL, Integer.toString(total));
        results.add(statistics);
        search.setSearchResults(results);
        return search;
    }

    @SuppressWarnings("unused")
    void printSuggestions(final StringBuilder[] suggestions) {
        for (final StringBuilder suggestion : suggestions) {
            logger.info("Suggestion : " + suggestion);
        }
    }

}