package ikube.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.listener.Event;
import ikube.listener.ListenerManager;
import ikube.toolkit.SerializationUtilities;

import java.util.List;
import java.util.Map;

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
		this.searcherWebService = new SearcherWebService();
		ListenerManager.fireEvent(Event.SEARCHER_OPENED, System.currentTimeMillis(), INDEX_CONTEXT, Boolean.FALSE);
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

	private void verifyResults(List<Map<String, String>> resultsList) {
		assertNotNull("Results should never be null : ", resultsList);
		assertTrue("There should always be at least one map in the results : ", resultsList.size() >= 1);
	}

}
