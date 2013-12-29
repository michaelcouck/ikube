package ikube.search;

import static ikube.toolkit.ObjectToolkit.populateFields;
import static junit.framework.Assert.assertEquals;
import ikube.AbstractTest;
import ikube.IConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.lucene.search.IndexSearcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class SearcherServiceTest extends AbstractTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SearcherServiceTest.class);

	@MockClass(realClass = Search.class)
	public class SearchComplexSortedMock {
		@Mock
		public ArrayList<HashMap<String, String>> execute() {
			HashMap<String, String> result = new HashMap<String, String>();
			result.put(IConstants.CONTENTS, IConstants.CONTENTS);
			result.put(IConstants.SCORE, Float.toString((float) Math.random()));

			HashMap<String, String> statistics = new HashMap<String, String>();
			statistics.put(IConstants.TOTAL, Long.toString(526));
			statistics.put(IConstants.DURATION, Long.toString(652));
			statistics.put(IConstants.SCORE, Float.toString(0.236f));

			ArrayList<HashMap<String, String>> searchResults = new ArrayList<HashMap<String, String>>();
			searchResults.add(result);
			searchResults.add(statistics);

			return searchResults;
		}
	}

	@MockClass(realClass = SearcherService.class)
	public class SearcherServiceMock {
		@Mock
		@SuppressWarnings("unchecked")
		protected <T> T getSearch(final Class<?> klass, final String indexName) throws Exception {
			Search search = (Search) Mockito.mock(klass);
			searches.add(search);
			return (T) search;
		}

		@Mock
		protected void persistSearch(final String indexName, final String[] searchStrings, final String[] searchStringsCorrected,
				final ArrayList<HashMap<String, String>> results) {
			// Nothing
		}
	}

	/** Class under test. */
	private SearcherService searcherService;

	private String indexName;
	private String[] searchStrings = new String[] { "hello" };
	private String[] searchFields = new String[] { IConstants.CONTENTS };
	private String[] typeFields = new String[] { "string" };
	private String[] sortFields = new String[] { IConstants.ID };
	private boolean fragment = true;
	private int firstResult = 0;
	private int maxResults = 10;
	private int distance = 10;
	private double latitude = 0.0;
	private double longitude = 0.0;
	private ikube.model.Search search;

	private List<Search> searches = new ArrayList<>();

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
		Mockit.tearDownMocks();
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
		searcherService.search(indexName, searchStrings, searchFields, typeFields, fragment, firstResult, maxResults, distance, latitude, longitude);
		verify();
	}

	@Test
	public void searchNumericRange() {
		searcherService.search(indexName, new String[] { "0.0-0.0" }, searchFields, typeFields, sortFields, fragment, firstResult, maxResults);
		verify();
	}

	@Test
	public void searchComplex() {
		searcherService.search(indexName, searchStrings, searchFields, typeFields, sortFields, fragment, firstResult, maxResults);
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
			@SuppressWarnings({ "unchecked" })
			protected <T> T getSearch(final Class<?> klass, final String indexName) throws Exception {
				return (T) klass.getConstructor(IndexSearcher.class).newInstance(indexSearcher);
			}
		};

		Mockito.when(monitorService.getIndexNames()).thenReturn(new String[] { "index-one", "index-two", "index-three", "index-four" });
		Mockito.when(monitorService.getIndexFieldNames(Mockito.anyString())).thenReturn(new String[] { "field-one", "field-two", "field-three", "field-four" });

		search.setCoordinate(null);
		Deencapsulation.setField(searcherService, dataBase);
		Deencapsulation.setField(searcherService, monitorService);
		ikube.model.Search searchResult = searcherService.searchAll(search);
		logger.info("Search : " + ToStringBuilder.reflectionToString(searchResult));
		int totalResultsPlusStats = monitorService.getIndexNames().length + 1;
		assertEquals("There should be a result for each index and the statistics : ", totalResultsPlusStats, searchResult.getSearchResults().size());
	}

	@Test
	public void persistSearch() {
		Mockit.tearDownMocks();
		searcherService = new SearcherService();
		Deencapsulation.setField(searcherService, dataBase);
		ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> result = new HashMap<String, String>();

		HashMap<String, String> statistics = new HashMap<String, String>();
		statistics.put(IConstants.TOTAL, "100");
		statistics.put(IConstants.DURATION, "100");
		statistics.put(IConstants.SCORE, "100");
		statistics.put(IConstants.SEARCH_STRINGS, "searchString");
		statistics.put(IConstants.CORRECTIONS, "correctedSearchString");

		results.add(result);
		results.add(statistics);

		searcherService.persistSearch(indexName, searchStrings, searchStrings, results);
		Mockito.verify(dataBase, Mockito.atLeast(1)).persist(Mockito.any(Search.class));
	}

	private void verify() {
		for (final Search search : searches) {
			Mockito.verify(search, Mockito.atLeastOnce()).execute();
		}
	}

}