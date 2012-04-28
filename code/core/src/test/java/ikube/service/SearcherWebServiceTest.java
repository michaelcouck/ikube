package ikube.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.IConstants;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.Search;
import ikube.search.spelling.SpellingChecker;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * These tests must just pass with out exception.
 * 
 * @author Michael Couck
 * @since 28.12.10
 * @version 01.00
 */
public class SearcherWebServiceTest extends ATest {

	private Search search = new Search();
	{
		search.setCount(10);
		search.setSearchStrings("hello world");
	}
	private SearcherWebService searcherWebService;

	public SearcherWebServiceTest() {
		super(SearcherWebServiceTest.class);
	}

	@Before
	public void before() throws Exception {
		Mockit.setUpMocks(ApplicationContextManagerMock.class);
		this.searcherWebService = new SearcherWebService();
		ApplicationContextManagerMock.setBean(indexContext);
		Deencapsulation.setField(searcherWebService, dataBase);
		SpellingChecker spellingChecker = new SpellingChecker();
		Deencapsulation.setField(spellingChecker, "languageWordListsDirectory", "./languageWordListsDirectory");
		Deencapsulation.setField(spellingChecker, "spellingIndexDirectoryPath", "./spellingIndexDirectoryPath");
		spellingChecker.initialize();
		Deencapsulation.setField(searcherWebService, spellingChecker);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ApplicationContextManager.class);
		ApplicationContextManagerMock.setBean(null);
		FileUtilities.deleteFile(new File("./languageWordListsDirectory"), 1);
		FileUtilities.deleteFile(new File("./spellingIndexDirectoryPath"), 1);
	}

	@Test
	public void searchSingle() {
		ArrayList<HashMap<String, String>> results = this.searcherWebService.searchSingle(indexContext.getIndexName(), "search string",
				IConstants.CONTENTS, Boolean.TRUE, 0, 10);
		logger.debug("Single search result : " + results);
		verifyResults(results);
	}

	@Test
	public void searchMulti() {
		ArrayList<HashMap<String, String>> results = this.searcherWebService.searchMulti(indexContext.getIndexName(), new String[] {
				"search", "strings" }, new String[] { IConstants.CONTENTS, IConstants.ID }, Boolean.TRUE, 0, 10);
		logger.debug("Multi search result : " + results);
		verifyResults(results);
	}

	@Test
	public void searchMultiSorted() {
		ArrayList<HashMap<String, String>> results = this.searcherWebService.searchMultiSorted(indexContext.getIndexName(), new String[] {
				"search", "strings" }, new String[] { IConstants.CONTENTS, IConstants.ID }, new String[] { "", "" }, Boolean.TRUE, 0, 10);
		logger.debug("Multi sorted search result : " + results);
		verifyResults(results);
	}

	@Test
	public void searchMultiAll() {
		ArrayList<HashMap<String, String>> results = this.searcherWebService.searchMultiAll(indexContext.getIndexName(), new String[] {
				"search", "strings" }, Boolean.TRUE, 0, 10);
		logger.debug("Multi sorted search result : " + results);
		verifyResults(results);
	}

	@Test
	public void searchSpatial() {
		ArrayList<HashMap<String, String>> results = this.searcherWebService.searchMultiSpacial(indexContext.getIndexName(), new String[] {
				"search", "strings" }, new String[] { IConstants.CONTENTS, IConstants.ID }, Boolean.TRUE, 0, 10, 10, 47.0008, 53.0001);
		logger.debug("Multi sorted search result : " + results);
		verifyResults(results);
	}

	@Test
	public void searchSpatialAll() {
		ArrayList<HashMap<String, String>> results = this.searcherWebService.searchMultiSpacialAll(indexContext.getIndexName(),
				new String[] { "search", "strings" }, Boolean.TRUE, 0, 10, 10, 47.0008, 53.0001);
		logger.debug("Multi sorted search result : " + results);
		verifyResults(results);
	}

	@Test
	public void ipAddress() throws Exception {
		InetAddress[] addresses = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
		if (addresses != null) {
			for (InetAddress inetAddress : addresses) {
				logger.info("Address : " + inetAddress);
			}
		}
		logger.info("Ip address : " + InetAddress.getLocalHost().getHostAddress());
	}

	@Test
	public void getMessageResults() {
		ArrayList<HashMap<String, String>> messageResults = this.searcherWebService.getMessageResults("indexName");
		assertNotNull("The message can't be null : ", messageResults);
		assertEquals("There should be one entry in the list : ", 1, messageResults.size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void addSearchStatistics() {
		when(dataBase.find(any(Class.class), anyString(), any(String[].class), any(Object[].class), anyInt(), anyInt())).thenReturn(
				Arrays.asList());
		this.searcherWebService.addSearchStatistics("indexName", new String[] { "hello world" }, 1, 1);
		Mockito.verify(dataBase, Mockito.atLeast(1)).persist(any(Search.class));

		when(dataBase.find(any(Class.class), anyString(), any(String[].class), any(Object[].class), anyInt(), anyInt())).thenReturn(
				Arrays.asList(search));
		this.searcherWebService.addSearchStatistics("indexName", new String[] { "hello world" }, 1, 1);
		Mockito.verify(dataBase, Mockito.atLeast(1)).merge(any(Search.class));
	}

	private void verifyResults(ArrayList<HashMap<String, String>> resultsList) {
		assertNotNull("Results should never be null : ", resultsList);
		assertTrue("There should always be at least one map in the results : ", resultsList.size() >= 1);
	}

}
