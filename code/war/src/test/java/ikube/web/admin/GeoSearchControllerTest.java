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
import ikube.toolkit.SerializationUtilities;
import ikube.web.MockFactory.ApplicationContextManagerMock;
import ikube.web.MockFactory.ServiceLocatorMock;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Couck
 * @since 14.01.2012
 * @version 01.00
 */
@Ignore
public class GeoSearchControllerTest {

	private static final Logger LOGGER = Logger.getLogger(GeoSearchControllerTest.class);

	/** Class under test. */
	private GeoSearchController geoSearchController;

	private HttpServletRequest request = mock(HttpServletRequest.class);
	private HttpServletResponse response = mock(HttpServletResponse.class);
	private ISearcherWebService searcherWebService = mock(ISearcherWebService.class);
	private IMonitorWebService monitorWebService = mock(IMonitorWebService.class);
	private IClusterManager clusterManager = mock(IClusterManager.class);
	private Server server = mock(Server.class);

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ServiceLocatorMock.class);
		geoSearchController = new GeoSearchController();
		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		parameterMap.put(IConstants.SEARCH_STRINGS, new String[] { IConstants.IKUBE });
		when(request.getParameterMap()).thenReturn(parameterMap);
		when(clusterManager.getServer()).thenReturn(server);
		when(monitorWebService.getIndexNames()).thenReturn(new String[] { "ikube", "ikube", "ikube" });
		Deencapsulation.setField(geoSearchController, clusterManager);
		Deencapsulation.setField(geoSearchController, monitorWebService);
		Deencapsulation.setField(geoSearchController, searcherWebService);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void handleRequest() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), "default.results.xml");
		String xml = FileUtilities.getContents(file, IConstants.ENCODING);
		ArrayList<HashMap<String, String>> results = (ArrayList<HashMap<String, String>>) SerializationUtilities.deserialize(xml);
		when(searcherWebService.searchMultiAll(IConstants.IKUBE, new String[] { IConstants.IKUBE }, true, 0, 10)).thenReturn(results);

		ModelAndView modelAndView = new ModelAndView();
		modelAndView = geoSearchController.search("targetView", "geospatial", "searchStrings", "10", "10", "10", modelAndView, request);
		Object total = modelAndView.getModel().get(IConstants.TOTAL);
		Object duration = modelAndView.getModel().get(IConstants.DURATION);
		results = (ArrayList<HashMap<String, String>>) modelAndView.getModel().get(IConstants.RESULTS);
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
		xml = FileUtilities.getContents(file, IConstants.ENCODING);
		results = (ArrayList<HashMap<String, String>>) SerializationUtilities.deserialize(xml);
		when(searcherWebService.searchMultiAll(IConstants.IKUBE, new String[] { IConstants.IKUBE }, true, 0, 10)).thenReturn(results);

		modelAndView = geoSearchController.handleRequest(request, response);

		total = modelAndView.getModel().get(IConstants.TOTAL);
		results = (ArrayList<HashMap<String, String>>) modelAndView.getModel().get(IConstants.RESULTS);
		firstResult = modelAndView.getModel().get(IConstants.FIRST_RESULT);
		maxResults = modelAndView.getModel().get(IConstants.MAX_RESULTS);

		assertEquals("Start result is 0 : ", 0, firstResult);
		assertEquals("End result is 10 : ", 10, maxResults);
		assertEquals("There are 3 * 3 results : ", 9, total);
		assertEquals("There are 2 * 3 results : ", 6, results.size());
	}

}