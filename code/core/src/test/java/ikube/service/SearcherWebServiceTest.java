package ikube.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.toolkit.ApplicationContextManager;
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
public class SearcherWebServiceTest extends BaseTest {

	private SearcherWebService searcherWebService;

	public SearcherWebServiceTest() {
		super(SearcherWebServiceTest.class);
	}

	@Before
	public void before() {
		this.searcherWebService = ApplicationContextManager.getBean(SearcherWebService.class);
		this.searcherWebService.setMultiSearcher(indexContext);
		this.indexContext.getIndex().setMultiSearcher(MULTI_SEARCHER);
		this.searcherWebService.setMultiSearcher(indexContext);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void searchSingle() {
		String result = this.searcherWebService.searchSingle(indexContext.getIndexName(), "search string", IConstants.CONTENTS,
				Boolean.TRUE, 0, 10);
		logger.debug("Single search result : " + result);
		List<Map<String, String>> resultsList = (List<Map<String, String>>) SerializationUtilities.deserialize(result);
		assertNotNull(resultsList);
		assertTrue(resultsList.size() >= 1);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void searchMulti() {
		String result = this.searcherWebService.searchMulti(indexContext.getIndexName(), new String[] { "search", "strings" },
				new String[] { IConstants.CONTENTS, IConstants.ID }, Boolean.TRUE, 0, 10);
		logger.debug("Multi search result : " + result);
		List<Map<String, String>> resultsList = (List<Map<String, String>>) SerializationUtilities.deserialize(result);
		assertNotNull(resultsList);
		assertTrue(resultsList.size() >= 1);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void searchMultiSorted() {
		String result = this.searcherWebService.searchMultiSorted(indexContext.getIndexName(), new String[] { "search", "strings" },
				new String[] { IConstants.CONTENTS, IConstants.ID }, new String[] { "", "" }, Boolean.TRUE, 0, 10);
		logger.debug("Multi sorted search result : " + result);
		List<Map<String, String>> resultsList = (List<Map<String, String>>) SerializationUtilities.deserialize(result);
		assertNotNull(resultsList);
		assertTrue(resultsList.size() >= 1);
	}

}
