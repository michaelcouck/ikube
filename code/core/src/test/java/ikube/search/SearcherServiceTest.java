package ikube.search;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.mock.SpellingCheckerMock;
import ikube.toolkit.ThreadUtilities;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.apache.lucene.search.IndexSearcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import static ikube.toolkit.ObjectToolkit.populateFields;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 02.00
 * @see ISearcherService
 * @since 21-11-2010
 */
@SuppressWarnings("deprecation")
public class SearcherServiceTest extends AbstractTest {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(SearcherServiceTest.class);

    @MockClass(realClass = Search.class)
    public static class SearchComplexSortedMock {
        @Mock
        public ArrayList<HashMap<String, String>> execute() {
            HashMap<String, String> result = new HashMap<>();
            result.put(IConstants.CONTENTS, IConstants.CONTENTS);
            result.put(IConstants.SCORE, Float.toString((float) Math.random()));

            HashMap<String, String> statistics = new HashMap<>();
            statistics.put(IConstants.TOTAL, Long.toString(526));
            statistics.put(IConstants.DURATION, Long.toString(652));
            statistics.put(IConstants.SCORE, Float.toString(0.236f));

            ArrayList<HashMap<String, String>> searchResults = new ArrayList<>();
            searchResults.add(result);
            searchResults.add(statistics);

            return searchResults;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @MockClass(realClass = SearcherService.class)
    public static class SearcherServiceMock {
        @Mock
        @SuppressWarnings({"unchecked", "UnusedParameters"})
        protected <T> T getSearch(final Class<?> klass, final String indexName) throws Exception {
            Search search = (Search) mock(klass);
            SEARCHES.add(search);
            return (T) search;
        }

        @Mock
        protected void persistSearch(final ikube.model.Search search) {
            // Nothing
        }
    }

    private static List<Search> SEARCHES = new ArrayList<>();

    /**
     * Class under test.
     */
    private SearcherService searcherService;
    private IClusterManager clusterManager;

    private String indexName;
    private int firstResult = 0;
    private int maxResults = 10;
    private boolean fragment = true;
    private ikube.model.Search search;
    private String[] searchStrings = new String[]{"hello"};
    private String[] searchFields = new String[]{IConstants.CONTENTS};
    private String[] typeFields = new String[]{"string"};
    private String[] sortFields = new String[]{IConstants.ID};

    @Before
    public void before() {
        Mockit.setUpMocks(new SearcherServiceMock(), new SearchComplexSortedMock());

        clusterManager = mock(IClusterManager.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                final Callable callable = (Callable) invocation.getArguments()[0];
                return ThreadUtilities.submit(IConstants.IKUBE, new Runnable() {
                    public void run() {
                        try {
                            callable.call();
                        } catch (Exception e) {
                            LOGGER.error("Error : ", e);
                        }
                    }
                });
            }
        }).when(clusterManager).sendTask(any(Callable.class));

        search = new ikube.model.Search();
        search = populateFields(new ikube.model.Search(), Boolean.TRUE, 10);
        search.setDistributed(Boolean.TRUE);

        searcherService = new SearcherService();
        indexName = indexContext.getIndexName();

        Deencapsulation.setField(searcherService, "clusterManager", clusterManager);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(SearcherServiceMock.class, SearchComplexSortedMock.class);
    }

    @Test
    public void searchSingle() {
        searcherService.search(indexName, searchStrings, searchFields, true, 0, 10);
        verify();
    }

    @Test
    public void searchMultiSorted() {
        searcherService.search(indexName, searchStrings, searchFields, typeFields, sortFields, fragment, firstResult, maxResults);
        verify();
    }

    @Test
    public void searchMultiSpacial() {
        int distance = 10;
        double latitude = 0.0;
        double longitude = 0.0;
        searcherService.search(indexName, searchStrings, searchFields, typeFields, fragment, firstResult, maxResults, distance, latitude, longitude);
        verify();
    }

    @Test
    public void searchNumericRange() {
        searcherService.search(indexName, new String[]{"0.0-0.0"}, searchFields, typeFields, sortFields, fragment, firstResult, maxResults);
        verify();
    }

    @Test
    public void searchComplex() {
        searcherService.search(indexName, searchStrings, searchFields, typeFields, sortFields, fragment, firstResult, maxResults);
        verify();
    }

    @Test
    public void searchAll() {
        searcherService.search(search);
        verify();
    }

    @Test
    public void searchAllIntegrate() {
        searcherService = new SearcherService() {
            @SuppressWarnings({"unchecked"})
            protected <T> T getSearch(final Class<?> klass, final String indexName) throws Exception {
                return (T) klass.getConstructor(IndexSearcher.class).newInstance(indexSearcher);
            }
        };
        Deencapsulation.setField(searcherService, "clusterManager", clusterManager);

        String[] indexes = {"index-one", "index-two", "index-three", "index-four"};
        String[] fields = {"field-one", "field-two", "field-three", "field-four"};
        when(monitorService.getIndexNames()).thenReturn(indexes);
        when(monitorService.getIndexFieldNames(anyString())).thenReturn(fields);

        search.setCoordinate(null);
        // Deencapsulation.setField(searcherService, dataBase);
        Deencapsulation.setField(searcherService, monitorService);
        ikube.model.Search searchResult = searcherService.searchAll(search);
        int totalResultsPlusStats = monitorService.getIndexNames().length + 1;
        assertEquals("There should be a result for each index and the statistics : ", totalResultsPlusStats,
                searchResult.getSearchResults().size());
    }

    @Test
    public void persistSearch() {
        try {
            // It must be tear down all
            Mockit.tearDownMocks();
            search.setIndexName(IConstants.GEOSPATIAL);
            search.setCount(0);

            SearcherService searcherService = new SearcherService();
            Deencapsulation.setField(searcherService, "clusterManager", clusterManager);
            when(clusterManager.get(anyString(), any())).thenReturn(search);

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

            search.setSearchStrings(Arrays.asList(searchStrings));
            search.setSearchResults(results);

            // indexName, searchStrings, searchStrings, results
            for (int i = 0; i < SearcherService.MAX_PERSIST_SIZE + 100; i++) {
                searcherService.persistSearch(search);
            }

            assertTrue(search.getCount() > 0);
            Mockito.verify(clusterManager, atLeastOnce()).put(any(), any(Serializable.class));
        } finally {
            // Set up the spelling mock for the next tests
            Mockit.setUpMocks(SpellingCheckerMock.class);
        }
    }

    private void verify() {
        for (final Search search : SEARCHES) {
            Mockito.verify(search, Mockito.atLeastOnce()).execute();
        }
    }

}