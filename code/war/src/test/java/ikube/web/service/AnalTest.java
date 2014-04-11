package ikube.web.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.model.Search;
import ikube.search.ISearcherService;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;
import ikube.web.service.Anal.TwitterSearch;
import ikube.web.toolkit.PerformanceTester;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static junit.framework.Assert.*;
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
        @SuppressWarnings({"unchecked", "UnusedParameters"})
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
    private ArrayList<HashMap<String, String>> results;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        gson = new GsonBuilder().disableHtmlEscaping().create();

        File file = FileUtilities.findFileRecursively(new File("."), "geospatial.results.xml");
        String xml = FileUtilities.getContent(file);
        results = (ArrayList<HashMap<String, String>>) SerializationUtilities.deserialize(xml);

        search = new TwitterSearch();
        search.setSearchStrings(new ArrayList<>(Arrays.asList("hello world")));
        search.setSearchFields(new ArrayList<>(Arrays.asList(IConstants.CONTENTS)));
        search.setOccurrenceFields(new ArrayList<>(Arrays.asList(Anal.OCCURRENCE)));
        search.setTypeFields(new ArrayList<>(Arrays.asList(IConstants.STRING)));
        search.setSearchResults(results);

        anal = mock(Anal.class);
        searcherService = mock(ISearcherService.class);

        when(anal.buildResponse()).thenCallRealMethod();
        when(anal.invertMatrix(any(Object[][].class))).thenCallRealMethod();
        when(anal.search(any(Search.class), anyInt(), anyInt(), anyString())).thenCallRealMethod();
        when(anal.buildJsonResponse(any(Object.class))).thenCallRealMethod();
        when(anal.twitter(any(HttpServletRequest.class))).thenCallRealMethod();
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
    @SuppressWarnings("unchecked")
    public void happy() {
        when(anal.happy(any(HttpServletRequest.class))).thenCallRealMethod();
        when(anal.heatMapData(any(ArrayList.class), anyInt())).thenReturn(new Object[0][]);
        Response response = anal.happy(null);
        String string = (String) response.getEntity();
        TwitterSearch twitterSearch = gson.fromJson(string, TwitterSearch.class);
        assertNotNull(twitterSearch);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void heatMapData() {
        when(anal.heatMapData(any(ArrayList.class), anyInt())).thenCallRealMethod();
        // Remove the statistics
        results.remove(results.size() - 1);
        // Add a lot more from this set to see the memory and performance
        final ArrayList<HashMap<String, String>> moreResults = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            moreResults.addAll(results);
        }
        search.setClusters(100);
        Object[][] heatMapData = anal.heatMapData(moreResults, search.getClusters());
        assertTrue("Must be less than the total results : ", heatMapData.length < moreResults.size());
        assertTrue("Must be less than the clustered capacity too : ", heatMapData.length <= search.getClusters());

        PerformanceTester.execute(new PerformanceTester.APerform() {
            @Override
            public void execute() throws Throwable {
                anal.heatMapData(moreResults, search.getClusters());
            }
        }, "Heat map data : ", 10);
    }

    @Test
    public void twitter() {
        Response response = anal.twitter(null);
        String string = (String) response.getEntity();
        TwitterSearch twitterSearch = gson.fromJson(string, TwitterSearch.class);
        assertNotNull(twitterSearch);
    }

    @Test
    public void timeLineSentiment() {
        search.setStartHour(-6);
        when(anal.setTimeLineSentiment(any(TwitterSearch.class))).thenCallRealMethod();
        when(anal.search(any(Search.class), anyInt(), anyLong(), anyLong(), anyInt(), any(Object[][].class))).thenCallRealMethod();
        Object[][] data = anal.setTimeLineSentiment(search);
        assertNotNull(data);
        assertEquals(7, data.length);
        assertEquals(3, data[0].length);
    }

    @Test
    public void count() {
        long startTime = 0;
        long endTime = 10;
        anal.search(search, startTime, endTime, IConstants.POSITIVE);
        assertEquals(3, search.getSearchStrings().size());
        assertEquals(3, search.getSearchFields().size());
        assertEquals(3, search.getOccurrenceFields().size());
        assertEquals(3, search.getTypeFields().size());

        Mockito.verify(searcherService, Mockito.atLeastOnce()).search((Search) Mockito.anyObject());
    }

}