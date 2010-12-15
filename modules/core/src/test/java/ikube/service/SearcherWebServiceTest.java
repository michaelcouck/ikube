package ikube.service;

import org.junit.Before;
import org.junit.Test;

import ikube.BaseTest;
import ikube.IConstants;
import ikube.toolkit.ApplicationContextManager;

public class SearcherWebServiceTest extends BaseTest {

	private SearcherWebService searcherWebService;

	@Before
	public void before() {
		this.searcherWebService = ApplicationContextManager.getBean(SearcherWebService.class);
		this.searcherWebService.setMultiSearcher(indexContext);
		this.indexContext.setMultiSearcher(multiSearcher);
	}

	@Test
	public void searchSingle() {
		String result = this.searcherWebService.searchSingle(indexContext.getIndexName(), "search string", IConstants.CONTENTS, Boolean.TRUE, 0,
				10);
		logger.debug("Single search result : " + result);
	}

	@Test
	public void searchMulti() {
		String result = this.searcherWebService.searchMulti(indexContext.getIndexName(), new String[] { "search", "strings" }, new String[] {
				IConstants.CONTENTS, IConstants.ID }, Boolean.TRUE, 0, 10);
		logger.debug("Multi search result : " + result);
	}

}
