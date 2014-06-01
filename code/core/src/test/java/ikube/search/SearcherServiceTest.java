package ikube.search;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.ThreadUtilities;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.apache.lucene.search.IndexSearcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;

import static ikube.toolkit.ObjectToolkit.populateFields;
import static junit.framework.Assert.assertEquals;
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

	@SuppressWarnings("UnusedDeclaration")
	@MockClass(realClass = SearcherService.class)
	public static class SearcherServiceMock {

		@Mock
		@SuppressWarnings({ "unchecked", "UnusedParameters" })
		protected <T> T getSearch(final Class<?> klass, final String indexName) throws Exception {
			Search search = (Search) mock(klass);
			when(search.execute()).thenReturn(getSearchResults());
			SEARCHES.add(search);
			return (T) search;
		}

		@Mock
		protected void persistSearch(final ikube.model.Search search) {
			// Nothing
		}

		private static ArrayList<HashMap<String, String>> getSearchResults() {
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
	private String[] searchStrings = new String[] { "hello" };
	private String[] searchFields = new String[] { IConstants.CONTENTS };
	private String[] typeFields = new String[] { "string" };
	private String[] sortFields = new String[] { IConstants.ID };

	@Before
	public void before() {
		Mockit.setUpMocks(new SearcherServiceMock(), new ApplicationContextManagerMock());

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
		when(clusterManager.getServer()).thenReturn(server);

		search = new ikube.model.Search();
		search = populateFields(new ikube.model.Search(), Boolean.TRUE, 10);
		search.setDistributed(Boolean.TRUE);
		search.setBoosts(Arrays.asList("1.0"));

		searcherService = new SearcherService();

		ApplicationContextManagerMock.setBean(ISearcherService.class, searcherService);

		indexName = indexContext.getIndexName();

		Deencapsulation.setField(searcherService, "clusterManager", clusterManager);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(SearcherService.class, ApplicationContextManager.class);
	}

	@Test
	public void searchSingle() {
		searcherService.search(indexName, searchStrings, searchFields, true, 0, 10);
		verifySearches();
	}

	@Test
	public void searchMultiSorted() {
		searcherService.search(indexName, searchStrings, searchFields, typeFields, sortFields, fragment, firstResult, maxResults);
		verifySearches();
	}

	@Test
	public void searchMultiSpacial() {
		int distance = 10;
		double latitude = 0.0;
		double longitude = 0.0;
		searcherService.search(indexName, searchStrings, searchFields, typeFields, fragment, firstResult, maxResults, distance, latitude, longitude);
		verifySearches();
	}

	@Test
	public void searchNumericRange() {
		searcherService.search(indexName, new String[] { "0.0-0.0" }, searchFields, typeFields, sortFields, fragment, firstResult, maxResults);
		verifySearches();
	}

	@Test
	public void searchComplex() {
		searcherService.search(indexName, searchStrings, searchFields, typeFields, sortFields, fragment, firstResult, maxResults);
		verifySearches();
	}

	@Test
	public void searchAll() {
		searcherService.search(search);
		verifySearches();
	}

	@Test
	public void searchAllIntegrate() {
		searcherService = new SearcherService() {
			@SuppressWarnings({ "unchecked" })
			protected <T> T getSearch(final Class<?> klass, final String indexName) throws Exception {
				return (T) klass.getConstructor(IndexSearcher.class).newInstance(indexSearcher);
			}
		};
		Deencapsulation.setField(searcherService, "clusterManager", clusterManager);

		String[] indexes = { "index-one", "index-two", "index-three", "index-four" };
		String[] fields = { "field-one", "field-two", "field-three", "field-four" };
		when(monitorService.getIndexNames()).thenReturn(indexes);
		when(monitorService.getIndexFieldNames(anyString())).thenReturn(fields);

		Map<String, Server> servers = new HashMap<>();
		servers.put("1", server);
		servers.put("2", server);
		servers.put("3", server);
		servers.put("4", server);
		servers.put("5", server);
		when(clusterManager.getServers()).thenReturn(servers);

		search.setCoordinate(null);
		Deencapsulation.setField(searcherService, monitorService);
		ikube.model.Search searchResult = searcherService.searchAll(search);
		int totalResultsPlusStats = monitorService.getIndexNames().length + 1;
		assertEquals("There should be a result for each index and the statistics : ", totalResultsPlusStats,
		  searchResult.getSearchResults().size());
	}

	@Test
	public void persistSearch() {
		Mockit.tearDownMocks(SearcherService.class, ApplicationContextManager.class);
        SearcherService searcherService = new SearcherService();
        Deencapsulation.setField(searcherService, "clusterManager", clusterManager);

        ArrayList<HashMap<String, String>> results = new ArrayList<>();
        HashMap<String, String> result = new HashMap<>();
        HashMap<String, String> statistics = new HashMap<>();

        search.setIndexName(IConstants.GEOSPATIAL);
        search.setCount(0);

        statistics.put(IConstants.TOTAL, "100");
        statistics.put(IConstants.DURATION, "100");
        statistics.put(IConstants.SCORE, "100");
        statistics.put(IConstants.SEARCH_STRINGS, "searchString");
        statistics.put(IConstants.CORRECTIONS, "correctedSearchString");

        when(clusterManager.get(anyString(), any())).thenReturn(search);

		results.add(result);
		results.add(statistics);

		search.setSearchStrings(Arrays.asList(searchStrings));
		search.setSearchResults(results);

		searcherService.persistSearch(search);

		verify(clusterManager, atLeastOnce()).put(any(String.class), any(Object.class), any(Serializable.class));

        for (int i = 0; i < 1000; i++) {
            searcherService.persistSearch(search);
        }

        assertEquals(1001, search.getCount());
	}

	private void verifySearches() {
		for (final Search search : SEARCHES) {
			verify(search, atLeastOnce()).execute();
		}
	}

}