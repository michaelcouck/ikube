package ikube.web.service;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Search;
import ikube.search.ISearcherService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Michael couck
 * @version 01.00
 * @since 01-03-2012
 */
public class AutoTest extends AbstractTest {

    @Spy
    @InjectMocks
    private Auto auto;
    @Mock
    private ISearcherService searcherService;

    @Test
    public void auto() {
        Search search = getSearch(new Search(), 2, "some", "fragment");
        search.setSearchStrings(Arrays.asList("search", "strings"));
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return new String[]{"suggestion"};
            }
        }).when(auto).suggestions(any(String.class), any(Search.class));
        auto.auto(search);
        assertEquals("suggestion", search.getSearchResults().get(0).get(IConstants.FRAGMENT));
    }

    @Test
    public void suggestions() throws Exception {
        String string = "hello AND world";
        Search search = new Search();
        search.setMaxResults(7);

        Search helloSearch = getSearch(new Search(), 2, "hello", "helloed");
        Search worldSearch = getSearch(new Search(), 2, "world", "worldly");
        when(searcherService.search(any(Search.class))).thenReturn(helloSearch, worldSearch);
        String[] suggestions = auto.suggestions(string, search);
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
        assertEquals(3, suggestions.length);
    }

    @Test
    public void multiSuggestions() {
        String[] expectedSuggestions = {
                "<b>hello</b> AND <b>world</b>",
                "<b>helloed</b> AND <b>worldly</b>",
                "<b>helloes</b> AND <b>worldliness</b>"};

        String string = "hello AND world";
        Search search = new Search();
        search.setMaxResults(7);

        Search helloSearch = getSearch(new Search(), 8, "hello", "helloed", "helloes");
        Search worldSearch = getSearch(new Search(), 8, "world", "worldly", "worldliness");
        when(searcherService.search(any(Search.class))).thenReturn(helloSearch, worldSearch);
        String[] suggestions = auto.suggestions(string, search);
        printSuggestions(suggestions);
        assertEquals(3, suggestions.length);
        for (int i = 0; i < suggestions.length; i++) {
            assertEquals(expectedSuggestions[i], suggestions[i]);
        }
    }

    private Search getSearch(final Search search, final int total, final String... fragments) {
        ArrayList<HashMap<String, String>> results = new ArrayList<>();
        for (final String fragment : fragments) {
            HashMap<String, String> result = new HashMap<>();
            result.put(IConstants.WORD, fragment);
            results.add(result);
        }
        HashMap<String, String> statistics = new HashMap<>();
        statistics.put(IConstants.TOTAL, Integer.toString(total));
        results.add(statistics);
        search.setSearchResults(results);
        return search;
    }

    @SuppressWarnings("unused")
    void printSuggestions(final String[] suggestions) {
        for (final String suggestion : suggestions) {
            logger.info("Suggestion : " + suggestion);
        }
    }

}