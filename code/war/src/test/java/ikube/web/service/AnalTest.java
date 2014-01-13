package ikube.web.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.model.Search;
import ikube.search.ISearcherService;
import ikube.toolkit.SerializationUtilities;
import ikube.web.service.Anal.TwitterSearch;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael couck
 * @version 01.00
 * @since 17.12.13
 */
public class AnalTest extends BaseTest {

    @MockClass(realClass = SerializationUtilities.class)
    public static class SerializationUtilitiesMock {
        @Mock
        @SuppressWarnings("unchecked")
        public static <T> T clone(final Class<T> klass, T t) {
            return (T) search;
        }
    }

    private static TwitterSearch search;

    /**
     * Class under test
     */
    private Anal anal;
    private Gson gson;
    private ISearcherService searcherService;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        gson = new GsonBuilder().disableHtmlEscaping().create();

        HashMap<String, String> statistics = new HashMap<>();
        statistics.put(IConstants.TOTAL, "100");
        ArrayList<HashMap<String, String>> searchResults = new ArrayList<>();

        search = new TwitterSearch();
        search.setSearchStrings(new ArrayList<>(Arrays.asList("hello world")));
        search.setSearchFields(new ArrayList<>(Arrays.asList(IConstants.CONTENTS)));
        search.setOccurrenceFields(new ArrayList<>(Arrays.asList(Anal.OCCURRENCE)));
        search.setTypeFields(new ArrayList<>(Arrays.asList(IConstants.STRING)));
        searchResults.add(statistics);
        search.setSearchResults(searchResults);

        anal = mock(Anal.class);
        searcherService = mock(ISearcherService.class);

        when(anal.buildResponse()).thenCallRealMethod();
        when(anal.invertMatrix(any(Object[][].class))).thenCallRealMethod();
        when(anal.count(any(Search.class), anyInt(), anyInt(), anyString())).thenCallRealMethod();
        when(anal.buildJsonResponse(any(Object.class))).thenCallRealMethod();
        when(anal.twitter(any(HttpServletRequest.class), any(UriInfo.class))).thenCallRealMethod();
        when(anal.unmarshall(any(Class.class), any(HttpServletRequest.class))).thenReturn(search);
        when(searcherService.search(any(Search.class))).thenReturn(search);

        Deencapsulation.setField(anal, gson);
        Deencapsulation.setField(anal, logger);
        Deencapsulation.setField(anal, searcherService);

        Mockit.setUpMock(SerializationUtilitiesMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(SerializationUtilitiesMock.class);
    }

    @Test
    public void analyze() {
        Response response = anal.twitter(null, null);
        String string = (String) response.getEntity();
        TwitterSearch twitterSearch = gson.fromJson(string, TwitterSearch.class);
        assertNotNull(twitterSearch);
    }

    @Test
    public void timeLineSentiment() {
        search.setStartHour("-6");
        when(anal.timeLineSentiment(any(TwitterSearch.class))).thenCallRealMethod();
        when(anal.search(any(Search.class), anyInt(), anyLong(), anyLong(), anyInt(), any(Object[][].class))).thenCallRealMethod();
        Object[][] data = anal.timeLineSentiment(search);
        for (final Object[] row : data) {
            logger.info(Arrays.deepToString(row));
        }
        assertNotNull(data);
        assertEquals(7, data.length);
        assertEquals(3, data[0].length);
    }

    @Test
    public void count() {
        long startTime = 0;
        long endTime = 10;
        anal.count(search, startTime, endTime, IConstants.POSITIVE);
        assertEquals(3, search.getSearchStrings().size());
        assertEquals(3, search.getSearchFields().size());
        assertEquals(3, search.getOccurrenceFields().size());
        assertEquals(3, search.getTypeFields().size());

        Mockito.verify(searcherService, Mockito.atLeastOnce()).search((Search) Mockito.anyObject());
    }

}