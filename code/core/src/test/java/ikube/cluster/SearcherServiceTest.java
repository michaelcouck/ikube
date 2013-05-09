package ikube.cluster;

import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.mock.ReaderUtilMock;
import ikube.mock.SpellingCheckerMock;
import ikube.search.SearcherService;

import java.util.ArrayList;
import java.util.HashMap;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.lucene.util.ReaderUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

	public SearcherServiceTest() {
		super(SearcherServiceTest.class);
	}

	@Before
	public void before() {
		searcherService = new SearcherService();
		Mockit.setUpMocks(SpellingCheckerMock.class, ReaderUtilMock.class);
		Deencapsulation.setField(searcherService, monitorService);
		Deencapsulation.setField(searcherService, dataBase);
		indexName = indexContext.getIndexName();
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ReaderUtil.class);
	}

	@Test
	public void searchSingle() {
		ArrayList<HashMap<String, String>> results = searcherService
				.searchSingle(indexName, searchStrings[0], searchFields[0], true, 0, 10);
		assertTrue(results.size() > 0);
	}

	@Test
	public void searchMulti() {
		ArrayList<HashMap<String, String>> results = searcherService.searchMulti(indexName, searchStrings, searchFields, fragment,
				firstResult, maxResults);
		assertTrue(results.size() > 0);
	}

	@Test
	public void searchMultiSorted() {
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSorted(indexName, searchStrings, searchFields, sortFields,
				fragment, firstResult, maxResults);
		assertTrue(results.size() > 0);
	}

	@Test
	public void searchMultiAll() {
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiAll(indexName, searchStrings, fragment, firstResult,
				maxResults);
		assertTrue(results.size() > 0);
	}

	@Test
	public void searchMultiSpacial() {
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSpacial(indexName, searchStrings, searchFields, fragment,
				firstResult, maxResults, distance, latitude, longitude);
		assertTrue(results.size() > 0);
	}

	@Test
	public void searchMultiSpacialAll() {
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSpacialAll(indexName, searchStrings, fragment, firstResult,
				maxResults, distance, latitude, longitude);
		assertTrue(results.size() > 0);
	}

	@Test
	public void searchNumericAll() {
		ArrayList<HashMap<String, String>> results = searcherService.searchNumericAll(indexName, searchStrings, fragment, firstResult,
				maxResults);
		assertTrue(results.size() > 0);
	}

	@Test
	public void searchNumericRange() {
		ArrayList<HashMap<String, String>> results = searcherService.searchNumericRange(indexName, new String[] { "0.0", "0.0" }, fragment,
				firstResult, maxResults);
		assertTrue(results.size() > 0);
	}

	@Test
	public void searchComplex() {
		ArrayList<HashMap<String, String>> results = searcherService.searchComplex(indexName, searchStrings, searchFields, typeFields,
				fragment, firstResult, maxResults);
		assertTrue(results.size() > 0);
	}

}