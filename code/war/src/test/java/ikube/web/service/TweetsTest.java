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

    private static SearchTwitter TWITTER_SEARCH;
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

        TWITTER_SEARCH = new SearchTwitter();
        TWITTER_SEARCH.setSearchStrings(new ArrayList<>(Arrays.asList("hello world")));
        TWITTER_SEARCH.setSearchFields(new ArrayList<>(Arrays.asList(IConstants.CONTENTS)));
        TWITTER_SEARCH.setOccurrenceFields(new ArrayList<>(Arrays.asList(IConstants.MUST)));
        TWITTER_SEARCH.setTypeFields(new ArrayList<>(Arrays.asList(IConstants.STRING)));
        TWITTER_SEARCH.setSearchResults(results);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return TWITTER_SEARCH;
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
        Response response = tweets.happy(TWITTER_SEARCH);
        SearchTwitter twitterSearch = (SearchTwitter) response.getEntity();
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
        TWITTER_SEARCH.setClusters(100);
        Object[][] heatMapData = tweets.heatMapData(moreResults, TWITTER_SEARCH.getClusters());
        assertTrue("Must be less than the total results : ", heatMapData.length < moreResults.size());
        assertTrue("Must be less than the clustered capacity too : ", heatMapData.length <= TWITTER_SEARCH.getClusters());

        execute(new PerformanceTester.APerform() {
            @Override
            public void execute() throws Throwable {
                tweets.heatMapData(moreResults, TWITTER_SEARCH.getClusters());
            }
        }, "Heat map data : ", 10);
    }

    @Test
    public void twitter() {
        Response response = tweets.twitter(TWITTER_SEARCH);
        SearchTwitter twitterSearch = (SearchTwitter) response.getEntity();
        assertNotNull(twitterSearch);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void timeLineSentiment() {
        TWITTER_SEARCH.setStartHour(-6);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(tweets).addCount(any(Object[].class), anyString(), any(HashMap.class));
        Object[][] data = tweets.setTimeLineSentiment(TWITTER_SEARCH);

        logger.error(Arrays.deepToString(data));

        assertNotNull(data);
        assertEquals(7, data.length);
        assertEquals(3, data[0].length);
    }

    @Test
    public void count() {
        long startTime = 0;
        long endTime = 10;
        tweets.search(TWITTER_SEARCH, startTime, endTime, IConstants.POSITIVE);
        assertEquals(3, TWITTER_SEARCH.getSearchStrings().size());
        assertEquals(3, TWITTER_SEARCH.getSearchFields().size());
        assertEquals(3, TWITTER_SEARCH.getOccurrenceFields().size());
        assertEquals(3, TWITTER_SEARCH.getTypeFields().size());

        verify(searcherService, atLeastOnce()).search((Search) anyObject());
    }

    @Test
    public void search() {
        int result = tweets.search(TWITTER_SEARCH, 0, 60, IConstants.POSITIVE);
        Assert.assertEquals(22, result);
    }

    @MockClass(realClass = SerializationUtilities.class)
    public static class SerializationUtilitiesMock {
        @Mock
        @SuppressWarnings({"unchecked", "UnusedParameters"})
        public static <T> T clone(final Class<T> klass, T t) {
            return (T) TWITTER_SEARCH;
        }
    }

}