package ikube.integration.service;

import static org.junit.Assert.assertTrue;
import ikube.action.Open;
import ikube.integration.AbstractIntegration;
import ikube.model.IndexContext;
import ikube.service.ISearcherService;
import ikube.service.SearcherService;
import ikube.toolkit.ApplicationContextManager;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearcherServiceIntegration extends AbstractIntegration {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearcherServiceIntegration.class);

	@Test
	@Ignore
	public void search() {
		String indexName = "wikiHistoryOne";
		IndexContext<?> indexContext = ApplicationContextManager.getBean(indexName);
		Open open = ApplicationContextManager.getBean(Open.class);
		open.executeInternal(indexContext);

		SearcherService searcherService = (SearcherService) ApplicationContextManager.getBean(ISearcherService.class);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiAll(indexName, new String[] { "hello" }, true, 0, 10);
		LOGGER.info("Results : " + results);
		assertTrue(results.size() > 1);
	}

}
