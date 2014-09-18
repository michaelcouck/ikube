package ikube.web.service;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Search;
import ikube.model.SearchTwitter;
import ikube.search.SearcherService;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.SerializationUtilities;
import ikube.web.toolkit.MockFactory;
import org.junit.After;
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

    private SearchTwitter searchTwitter;
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

        searchTwitter = new SearchTwitter();
        searchTwitter.setSearchStrings(new ArrayList<>(Arrays.asList("hello world")));
        searchTwitter.setSearchFields(new ArrayList<>(Arrays.asList(IConstants.CONTENTS)));
        searchTwitter.setOccurrenceFields(new ArrayList<>(Arrays.asList(IConstants.MUST)));
        searchTwitter.setTypeFields(new ArrayList<>(Arrays.asList(IConstants.STRING)));
        searchTwitter.setSearchResults(results);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return searchTwitter;
            }
        }).when(searcherService).search(any(Search.class));

        setField(tweets, logger);

        setUpMock(MockFactory.SerializationUtilitiesMock.class);
        MockFactory.setMock(SearchTwitter.class, searchTwitter);
    }

    @After
    public void after() {
        tearDownMocks(MockFactory.SerializationUtilitiesMock.class);
        MockFactory.removeMock(SearchTwitter.class);
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
        Response response = tweets.happy(searchTwitter);
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
        searchTwitter.setClusters(100);
        Object[][] heatMapData = tweets.heatMapData(moreResults, searchTwitter.getClusters());
        assertTrue("Must be less than the total results : ", heatMapData.length < moreResults.size());
        assertTrue("Must be less than the clustered capacity too : ", heatMapData.length <= searchTwitter.getClusters());

        PerformanceTester.execute(new PerformanceTester.APerform() {
            @Override
            public void execute() throws Throwable {
                tweets.heatMapData(moreResults, searchTwitter.getClusters());
            }
        }, "Heat map data ", 10, false);
    }

    @Test
    public void twitter() {
        Response response = tweets.twitter(searchTwitter);
        SearchTwitter twitterSearch = (SearchTwitter) response.getEntity();
        assertNotNull(twitterSearch);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void timeLineSentiment() {
        searchTwitter.setStartHour(-6);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(tweets).addCount(any(Object[].class), anyString(), any(HashMap.class));
        Object[][] data = tweets.setTimeLineSentiment(searchTwitter);

        logger.error(Arrays.deepToString(data));

        assertNotNull(data);
        assertEquals(7, data.length);
        assertEquals(3, data[0].length);
    }

    @Test
    public void count() {
        long startTime = 0;
        long endTime = 10;
        int total = tweets.search(searchTwitter, startTime, endTime, IConstants.POSITIVE);
        assertEquals(22, total);
        verify(searcherService, atLeastOnce()).search((Search) anyObject());
    }

    @Test
    public void search() {
        int result = tweets.search(searchTwitter, 0, 60, IConstants.POSITIVE);
        assertEquals(22, result);
    }

}