package ikube.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.mock.ApplicationContextManagerMock;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.SerializationUtilities;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

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

	private SearcherWebService searcherWebService;

	public SearcherWebServiceTest() {
		super(SearcherWebServiceTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class);
		ApplicationContextManagerMock.BEAN = INDEX_CONTEXT;
		this.searcherWebService = new SearcherWebService();
		((SearcherWebService) this.searcherWebService).setSearchDelegate(new SearchDelegate());
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ApplicationContextManager.class);
		ApplicationContextManagerMock.BEAN = null;
	}

	@Test
	@SuppressWarnings("unchecked")
	public void searchSingle() {
		String result = this.searcherWebService.searchSingle(INDEX_CONTEXT.getIndexName(), "search string", IConstants.CONTENTS,
				Boolean.TRUE, 0, 10);
		logger.debug("Single search result : " + result);
		List<Map<String, String>> resultsList = (List<Map<String, String>>) SerializationUtilities.deserialize(result);
		verifyResults(resultsList);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void searchMulti() {
		String result = this.searcherWebService.searchMulti(INDEX_CONTEXT.getIndexName(), new String[] { "search", "strings" },
				new String[] { IConstants.CONTENTS, IConstants.ID }, Boolean.TRUE, 0, 10);
		logger.debug("Multi search result : " + result);
		List<Map<String, String>> resultsList = (List<Map<String, String>>) SerializationUtilities.deserialize(result);
		verifyResults(resultsList);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void searchMultiSorted() {
		String result = this.searcherWebService.searchMultiSorted(INDEX_CONTEXT.getIndexName(), new String[] { "search", "strings" },
				new String[] { IConstants.CONTENTS, IConstants.ID }, new String[] { "", "" }, Boolean.TRUE, 0, 10);
		logger.debug("Multi sorted search result : " + result);
		List<Map<String, String>> resultsList = (List<Map<String, String>>) SerializationUtilities.deserialize(result);
		verifyResults(resultsList);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void searchMultiAll() {
		String result = this.searcherWebService.searchMultiAll(INDEX_CONTEXT.getIndexName(), new String[] { "search", "strings" },
				Boolean.TRUE, 0, 10);
		logger.debug("Multi sorted search result : " + result);
		List<Map<String, String>> resultsList = (List<Map<String, String>>) SerializationUtilities.deserialize(result);
		verifyResults(resultsList);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void searchSpatial() {
		String result = this.searcherWebService.searchSpacialMulti(INDEX_CONTEXT.getIndexName(), new String[] { "search", "strings" },
				new String[] { IConstants.CONTENTS, IConstants.ID }, Boolean.TRUE, 0, 10, 10, 47.0008, 53.0001);
		logger.debug("Multi sorted search result : " + result);
		List<Map<String, String>> resultsList = (List<Map<String, String>>) SerializationUtilities.deserialize(result);
		verifyResults(resultsList);
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
		List<Map<String, String>> messageResults = this.searcherWebService.getMessageResults("indexName");
		assertNotNull("The message can't be null : ", messageResults);
		assertEquals("There should be one entry in the list : ", 1, messageResults.size());
	}

	private void verifyResults(List<Map<String, String>> resultsList) {
		assertNotNull("Results should never be null : ", resultsList);
		assertTrue("There should always be at least one map in the results : ", resultsList.size() >= 1);
	}

}
