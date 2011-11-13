package ikube.web.admin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.Server;
import ikube.service.IMonitorWebService;
import ikube.service.ISearcherWebService;
import ikube.toolkit.FileUtilities;
import ikube.web.MockFactory.ApplicationContextManagerMock;
import ikube.web.MockFactory.ServiceLocatorMock;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

public class SearchControllerTest {

	private static final Logger LOGGER = Logger.getLogger(SearchControllerTest.class);

	/** Class under test. */
	private SearchController searchController;

	private HttpServletRequest request = mock(HttpServletRequest.class);
	private HttpServletResponse response = mock(HttpServletResponse.class);

	@Before
	public void before() {
		searchController = new SearchController();
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ServiceLocatorMock.class);
		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		parameterMap.put(IConstants.SEARCH_STRINGS, new String[] { IConstants.IKUBE });
		when(request.getParameterMap()).thenReturn(parameterMap);
		Server server = mock(Server.class);
		IClusterManager clusterManager = mock(IClusterManager.class); // ApplicationContextManagerMock.getBean(IClusterManager.class);
		when(clusterManager.getServer()).thenReturn(server);
		IMonitorWebService monitorWebService = mock(IMonitorWebService.class); // ApplicationContextManagerMock.getBean(IMonitorWebService.class);
		when(monitorWebService.getIndexNames()).thenReturn(new String[] { "ikube", "ikube", "ikube" });
		Deencapsulation.setField(searchController, clusterManager);
		Deencapsulation.setField(searchController, monitorWebService);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void handleRequest() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), "default.results.xml");
		String xml = FileUtilities.getContents(file, Integer.MAX_VALUE, IConstants.ENCODING);

		ISearcherWebService searcherWebService = ServiceLocatorMock.getService(ISearcherWebService.class, null, null, null);
		when(searcherWebService.searchMultiAll(IConstants.IKUBE, new String[] { IConstants.IKUBE }, true, 0, 10)).thenReturn(xml);

		ModelAndView modelAndView = searchController.handleRequest(request, response);
		Object total = modelAndView.getModel().get(IConstants.TOTAL);
		Object duration = modelAndView.getModel().get(IConstants.DURATION);
		List<Map<String, String>> results = (List<Map<String, String>>) modelAndView.getModel().get(IConstants.RESULTS);
		Object corrections = modelAndView.getModel().get(IConstants.CORRECTIONS);
		Object searchStrings = modelAndView.getModel().get(IConstants.SEARCH_STRINGS);
		Object firstResult = modelAndView.getModel().get(IConstants.FIRST_RESULT);
		Object maxResults = modelAndView.getModel().get(IConstants.MAX_RESULTS);
		Object server = modelAndView.getModel().get(IConstants.SERVER);

		LOGGER.info(total);
		LOGGER.info(duration);
		LOGGER.info(corrections);
		LOGGER.info(searchStrings);
		LOGGER.info(firstResult);
		LOGGER.info(maxResults);
		LOGGER.info(server);
		LOGGER.info(results.size());

		assertEquals("Start result is 0 : ", 0, firstResult);
		assertEquals("End result is 10 : ", 10, maxResults);
		assertEquals("There are 11 * 3 results : ", 33, total);
		assertEquals("There are 10 * 3 results : ", 10, results.size());

		file = FileUtilities.findFileRecursively(new File("."), "default.results.small.xml");
		xml = FileUtilities.getContents(file, Integer.MAX_VALUE, IConstants.ENCODING);
		when(searcherWebService.searchMultiAll(IConstants.IKUBE, new String[] { IConstants.IKUBE }, true, 0, 10)).thenReturn(xml);

		modelAndView = searchController.handleRequest(request, response);

		total = modelAndView.getModel().get(IConstants.TOTAL);
		results = (List<Map<String, String>>) modelAndView.getModel().get(IConstants.RESULTS);
		firstResult = modelAndView.getModel().get(IConstants.FIRST_RESULT);
		maxResults = modelAndView.getModel().get(IConstants.MAX_RESULTS);

		assertEquals("Start result is 0 : ", 0, firstResult);
		assertEquals("End result is 10 : ", 10, maxResults);
		assertEquals("There are 3 * 3 results : ", 9, total);
		assertEquals("There are 2 * 3 results : ", 6, results.size());
	}

}