package ikube.search;

import ikube.AbstractTest;
import ikube.IConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.lucene.util.ReaderUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearcherServiceTest extends AbstractTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SearcherServiceTest.class);

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

	private List<Search> searches = new ArrayList<>();

	@Before
	public void before() {
		searcherService = new SearcherService() {
			@SuppressWarnings("unchecked")
			protected <T> T getSearch(final Class<?> klass, final String indexName) throws Exception {
				Search search = (Search) Mockito.mock(klass);
				searches.add(search);
				return (T) search;
			}

			protected void persistSearch(final String indexName, final String[] searchStrings, final String[] searchStringsCorrected,
					final ArrayList<HashMap<String, String>> results) {
				// Nothing
			}

		};
		indexName = indexContext.getIndexName();
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ReaderUtil.class);
	}

	@Test
	public void searchSingle() {
		searcherService.searchSingle(indexName, searchStrings[0], searchFields[0], true, 0, 10);
		verify();
	}

	@Test
	public void searchMulti() {
		searcherService.searchMulti(indexName, searchStrings, searchFields, fragment, firstResult, maxResults);
		verify();
	}

	@Test
	public void searchMultiSorted() {
		searcherService.searchMultiSorted(indexName, searchStrings, searchFields, sortFields, fragment, firstResult, maxResults);
		verify();
	}

	@Test
	public void searchMultiAll() {
		searcherService.searchMultiAll(indexName, searchStrings, fragment, firstResult, maxResults);
		verify();
	}

	@Test
	public void searchMultiSpacial() {
		searcherService.searchMultiSpacial(indexName, searchStrings, searchFields, fragment, firstResult, maxResults, distance, latitude, longitude);
		verify();
	}

	@Test
	public void searchMultiSpacialAll() {
		searcherService.searchMultiSpacialAll(indexName, searchStrings, fragment, firstResult, maxResults, distance, latitude, longitude);
		verify();
	}

	@Test
	public void searchNumericAll() {
		searcherService.searchNumericAll(indexName, searchStrings, fragment, firstResult, maxResults);
		verify();
	}

	@Test
	public void searchNumericRange() {
		searcherService.searchNumericRange(indexName, new String[] { "0.0", "0.0" }, fragment, firstResult, maxResults);
		verify();
	}

	@Test
	public void searchComplex() {
		searcherService.searchComplex(indexName, searchStrings, searchFields, typeFields, fragment, firstResult, maxResults);
		verify();
	}

	@Test
	public void persistSearch() {
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