package ikube.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.database.jpa.ADataBaseJpa;
import ikube.database.jpa.DataBaseJpa;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.Search;
import ikube.toolkit.ApplicationContextManager;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * These tests must just pass with out exception.
 * 
 * @author Michael Couck
 * @since 28.12.10
 * @version 01.00
 */
public class SearcherWebServiceTest extends ATest {
	
	@MockClass(realClass = ADataBaseJpa.class)
	public static class IDataBaseMock {
		@Mock()
		@SuppressWarnings("unchecked")
		public <T> T find(Class<T> klass, String sql, String[] names, Object[] values) {
			Search search = new Search();
			search.setCount(10);
			search.setSearchStrings("hello world");
			return (T) search;
		}
		@Mock()
		public <T> T merge(T object) {
			return object;
		}
		@Mock()
		public <T> T persist(T object) {
			return object;
		}
	}

	private SearcherWebService searcherWebService;

	public SearcherWebServiceTest() {
		super(SearcherWebServiceTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, IDataBaseMock.class );
		this.searcherWebService = new SearcherWebService();
		ApplicationContextManagerMock.setBean(indexContext);
		Deencapsulation.setField(searcherWebService, new DataBaseJpa());
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ApplicationContextManager.class);
		ApplicationContextManagerMock.setBean(null);
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
	public void addSearchStatistics() {
		String[] searchStrings = new String[] { "hello world" };
		this.searcherWebService.addSearchStatistics(searchStrings);
	}

	private void verifyResults(ArrayList<HashMap<String, String>> resultsList) {
		assertNotNull("Results should never be null : ", resultsList);
		assertTrue("There should always be at least one map in the results : ", resultsList.size() >= 1);
	}
	
}
