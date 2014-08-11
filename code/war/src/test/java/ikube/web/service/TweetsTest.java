package ikube.web.service;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Search;
import ikube.model.SearchTwitter;
import ikube.search.SearcherService;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;
import ikube.web.toolkit.PerformanceTester;
import mockit.Mock;
import mockit.MockClass;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static ikube.web.toolkit.PerformanceTester.execute;
import static junit.framework.Assert.*;
import static mockit.Deencapsulation.setField;
import static mockit.Mockit.setUpMock;
import static mockit.Mockit.tearDownMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Michael couck
 * @version 01.00
 * @since 17-12-2013
 */
public class TweetsTest extends AbstractTest {

    private static SearchTwitter search;
    /**
     * Class under test
     */
    @Spy
    @InjectMocks
    private Tweets tweets;
    @org.mockito.Mock
    private SearcherService searcherService;
    private ArrayList<HashMap<String, String>> results;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        File file = FileUtilities.findFileRecursively(new File("."), "geospatial.results.xml");
        String xml = FileUtilities.getContent(file);
        results = (ArrayList<HashMap<String, String>>) SerializationUtilities.deserialize(xml);

        search = new SearchTwitter();
        search.setSearchStrings(new ArrayList<>(Arrays.asList("hello world")));
        search.setSearchFields(new ArrayList<>(Arrays.asList(IConstants.CONTENTS)));
        search.setOccurrenceFields(new ArrayList<>(Arrays.asList(IConstants.MUST)));
        search.setTypeFields(new ArrayList<>(Arrays.asList(IConstants.STRING)));
        search.setSearchResults(results);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return search;
            }
        }).when(tweets).unmarshall(any(Class.class), any(HttpServletRequest.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return search;
            }
        }).when(searcherService).search(any(Search.class));

        setField(tweets, logger);

        setUpMock(SerializationUtilitiesMock.class);
    }

    @After
    public void after() {
        tearDownMocks(SerializationUtilitiesMock.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void happy() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return new Object[0][];
            }
        }).when(tweets).heatMapData(any(ArrayList.class), anyInt());
        Response response = tweets.happy(null);
        String string = (String) response.getEntity();
        SearchTwitter twitterSearch = IConstants.GSON.fromJson(string, SearchTwitter.class);
        assertNotNull(twitterSearch);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void heatMapData() {
        // Remove the statistics
        results.remove(results.size() - 1);
        // Add a lot more from this set to see the memory and performance
        final ArrayList<HashMap<String, String>> moreResults = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            moreResults.addAll(results);
        }
        search.setClusters(100);
        Object[][] heatMapData = tweets.heatMapData(moreResults, search.getClusters());
        assertTrue("Must be less than the total results : ", heatMapData.length < moreResults.size());
        assertTrue("Must be less than the clustered capacity too : ", heatMapData.length <= search.getClusters());

        execute(new PerformanceTester.APerform() {
            @Override
            public void execute() throws Throwable {
                tweets.heatMapData(moreResults, search.getClusters());
            }
        }, "Heat map data : ", 10);
    }

    @Test
    public void twitter() {
        Response response = tweets.twitter(null);
        String string = (String) response.getEntity();
        SearchTwitter twitterSearch = IConstants.GSON.fromJson(string, SearchTwitter.class);
        assertNotNull(twitterSearch);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void timeLineSentiment() {
        search.setStartHour(-6);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(tweets).addCount(any(Object[].class), anyString(), any(HashMap.class));
        Object[][] data = tweets.setTimeLineSentiment(search);

        logger.error(Arrays.deepToString(data));

        assertNotNull(data);
        assertEquals(7, data.length);
        assertEquals(3, data[0].length);
    }

    @Test
    public void count() {
        long startTime = 0;
        long endTime = 10;
        tweets.search(search, startTime, endTime, IConstants.POSITIVE);
        assertEquals(3, search.getSearchStrings().size());
        assertEquals(3, search.getSearchFields().size());
        assertEquals(3, search.getOccurrenceFields().size());
        assertEquals(3, search.getTypeFields().size());

        verify(searcherService, atLeastOnce()).search((Search) anyObject());
    }

    @Test
    public void search() {
        int result = tweets.search(search, 0, 60, IConstants.POSITIVE);
        Assert.assertEquals(22, result);
    }

    @MockClass(realClass = SerializationUtilities.class)
    public static class SerializationUtilitiesMock {
        @Mock
        @SuppressWarnings({"unchecked", "UnusedParameters"})
        public static <T> T clone(final Class<T> klass, T t) {
            return (T) search;
        }
    }

}