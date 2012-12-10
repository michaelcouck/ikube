package ikube.service;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.mock.ReaderUtilMock;
import ikube.search.spelling.SpellingChecker;
import ikube.service.SearcherService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearcherServiceTest extends ATest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SearcherServiceTest.class);

	@Cascading
	private SpellingChecker spellingChecker;

	/** Class under test. */
	private SearcherService searcherService;

	private String indexName;
	private String[] searchStrings = new String[] { "hello" };
	private String[] searchFields = new String[] { IConstants.CONTENTS };
	private String[] sortFields = new String[] { IConstants.ID };
	private boolean fragment = true;
	private int firstResult = 0;
	private int maxResults = 10;
	private int distance = 10;
	private double latitude = 0.0;
	private double longitude = 0.0;

	public SearcherServiceTest() {
		super(SearcherServiceTest.class);
	}

	@Before
	public void before() {
		searcherService = new SearcherService();
		
		Mockit.setUpMocks();
		Mockit.setUpMocks(ReaderUtilMock.class);
		Deencapsulation.setField(searcherService, monitorService);
		indexName = indexContext.getIndexName();
	}

	private void initialize() {
		Deencapsulation.setField(searcherService, dataBase);
		Deencapsulation.setField(searcherService, spellingChecker);
	}

	@Test
	public void searchSingle() {
		initialize();
		ArrayList<HashMap<String, String>> results = searcherService
				.searchSingle(indexName, searchStrings[0], searchFields[0], true, 0, 10);
		verifyResults(results);
	}

	@Test
	public void searchMulti() {
		initialize();
		ArrayList<HashMap<String, String>> results = searcherService.searchMulti(indexName, searchStrings, searchFields, fragment,
				firstResult, maxResults);
		verifyResults(results);
	}

	@Test
	public void searchMultiSorted() {
		initialize();
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSorted(indexName, searchStrings, searchFields, sortFields,
				fragment, firstResult, maxResults);
		verifyResults(results);
	}

	@Test
	public void searchMultiAll() {
		initialize();
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiAll(indexName, searchStrings, fragment, firstResult,
				maxResults);
		logger.info("Results : " + results);
		verifyResults(results);
	}

	@Test
	public void searchMultiSpacial() {
		initialize();
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSpacial(indexName, searchStrings, searchFields, fragment,
				firstResult, maxResults, distance, latitude, longitude);
		verifyResults(results);
	}

	@Test
	public void searchMultiSpacialAll() {
		initialize();
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSpacialAll(indexName, searchStrings, fragment, firstResult,
				maxResults, distance, latitude, longitude);
		verifyResults(results);
	}
	
	@Test
	public void addSearchStatistics() {
		// TODO Implement this test
	}

	private void verifyResults(final ArrayList<HashMap<String, String>> results) {
		assertTrue(results.size() > 0);
		Map<String, String> statistics = results.get(results.size() - 1);
		assertTrue("The search strings should be in the statistics map : ",
				statistics.get(IConstants.SEARCH_STRINGS).contains(searchStrings[0]));
	}

}