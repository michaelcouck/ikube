package ikube.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ikube.BaseTest;
import ikube.service.ISearcherWebServiceExecuter;
import ikube.service.SearcherWebServiceExecuter;
import ikube.toolkit.ApplicationContextManager;

import java.util.List;
import java.util.Map;

import org.junit.Test;



public class WebServicePublisherTest extends BaseTest {

	@Test
	public void execute() throws Exception {
		ISearcherWebServiceExecuter searcherWebServiceExecuter = ApplicationContextManager.getBean(SearcherWebServiceExecuter.class);
		List<Map<String, String>> results = searcherWebServiceExecuter.execute();
		assertNotNull(results);
		assertTrue(results.size() > 0);
	}

}
