package ikube.search;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.mock.SpellingCheckerMock;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.lucene.search.IndexSearcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static ikube.toolkit.ObjectToolkit.populateFields;
import static junit.framework.Assert.assertEquals;

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
			searches.add(search);
			return (T) search;
		}

		@Mock
		protected void persistSearch(final ikube.model.Search search) {
			// Nothing
		}
	}

	/**
	 * Class under test.
	 */
	private SearcherService searcherService;

	private String indexName;
	private String[] searchStrings = new String[]{"hello"};
	private String[] searchFields = new String[]{IConstants.CONTENTS};
	private String[] typeFields = new String[]{"string"};
	private String[] sortFields = new String[]{IConstants.ID};
	private boolean fragment = true;
	private int firstResult = 0;
	private int maxResults = 10;
    private ikube.model.Search search;

	private static List<Search> searches = new ArrayList<>();

	@Before
	public void before() {
		search = new ikube.model.Search();
		search = populateFields(new ikube.model.Search(), Boolean.TRUE, 10);

		Mockit.setUpMocks(new SearcherServiceMock(), new SearchComplexSortedMock());
		searcherService = new SearcherService();
		indexName = indexContext.getIndexName();
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
		searcherService.search(indexName, searchStrings, searchFields, typeFields, sortFields, fragment, firstResult,
			maxResults);
		verify();
	}

	@Test
	public void searchMultiSpacial() {
        int distance = 10;
        double latitude = 0.0;
        double longitude = 0.0;
        searcherService.search(indexName, searchStrings, searchFields, typeFields, fragment, firstResult, maxResults,
            distance, latitude, longitude);
		verify();
	}

	@Test
	public void searchNumericRange() {
		searcherService.search(indexName, new String[]{"0.0-0.0"}, searchFields, typeFields, sortFields, fragment,
			firstResult, maxResults);
		verify();
	}

	@Test
	public void searchComplex() {
		searcherService.search(indexName, searchStrings, searchFields, typeFields, sortFields, fragment, firstResult,
			maxResults);
		verify();
	}

	@Test
	public void searchComplexSortedJson() {
		searcherService.search(search);
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

		when(monitorService.getIndexNames()).thenReturn(new String[]{"index-one", "index-two", "index-three",
			"index-four"});
		when(monitorService.getIndexFieldNames(anyString())).thenReturn(new String[]{"field-one",
			"field-two", "field-three", "field-four"});

		search.setCoordinate(null);
		Deencapsulation.setField(searcherService, dataBase);
		Deencapsulation.setField(searcherService, monitorService);
		ikube.model.Search searchResult = searcherService.searchAll(search);
		logger.info("Search : " + ToStringBuilder.reflectionToString(searchResult));
		int totalResultsPlusStats = monitorService.getIndexNames().length + 1;
		assertEquals("There should be a result for each index and the statistics : ", totalResultsPlusStats,
			searchResult.getSearchResults().size());
	}

	@Test
	public void persistSearch() {
		try {
			// IT must be tear down all
			Mockit.tearDownMocks();
            search.setIndexName(IConstants.GEOSPATIAL);

			IDataBase dataBase = mock(IDataBase.class);
			SearcherService searcherService = new SearcherService();
			Deencapsulation.setField(searcherService, dataBase);
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
            for (int i = 0; i < SearcherService.MAX_PERSIST_SIZE + 100;  i++) {
                searcherService.persistSearch(search);
            }
			Mockito.verify(dataBase, atLeastOnce()).persistBatch(any(List.class));

            when(dataBase.findCriteria(any(Class.class), any(String[].class), any(Object[].class))).thenReturn(search);
            for (int i = 0; i < SearcherService.MAX_MERGE_SIZE + 100;  i++) {
                searcherService.persistSearch(search);
            }
            Mockito.verify(dataBase, atLeastOnce()).persistBatch(any(List.class));
		} finally {
			// Set up the spelling mock for the next tests
			Mockit.setUpMocks(SpellingCheckerMock.class);
		}
	}

	private void verify() {
		for (final Search search : searches) {
			Mockito.verify(search, Mockito.atLeastOnce()).execute();
		}
	}

}