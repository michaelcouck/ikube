package ikube.web.admin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.Server;
import ikube.service.ISearcherWebService;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;
import ikube.web.MockFactory.ApplicationContextManagerMock;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Couck
 * @since 14.01.2012
 * @version 01.00
 */
public class GeoSearchControllerTest {

	private static final Logger LOGGER = Logger.getLogger(GeoSearchControllerTest.class);

	/** Class under test. */
	private GeoSearchController geoSearchController;

	private Server server;
	private IClusterManager clusterManager;
	private ISearcherWebService searcherWebService;

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class);
		geoSearchController = new GeoSearchController();
		server = mock(Server.class);
		clusterManager = mock(IClusterManager.class);
		searcherWebService = mock(ISearcherWebService.class);
		when(clusterManager.getServer()).thenReturn(server);
		Deencapsulation.setField(geoSearchController, clusterManager);
		Deencapsulation.setField(geoSearchController, searcherWebService);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void search() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), "default.results.xml");
		String xml = FileUtilities.getContents(file, IConstants.ENCODING);
		ArrayList<HashMap<String, String>> results = (ArrayList<HashMap<String, String>>) SerializationUtilities.deserialize(xml);
		when(
				searcherWebService.searchMultiSpacialAll(anyString(), any(String[].class), anyBoolean(), anyInt(), anyInt(), anyInt(),
						anyDouble(), anyDouble())).thenReturn(results);

		int distanceInt = 10;
		int firstResultInt = 0;
		int maxResultsInt = 10;
		double latitudeDouble = 10;
		double longitudeDouble = 10;
		int resultsSizeInt = 11;

		ModelAndView modelAndView = new ModelAndView();
		modelAndView = geoSearchController.search("geospatial", "searchStrings", latitudeDouble, longitudeDouble, distanceInt,
				firstResultInt, maxResultsInt, "", modelAndView);
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

		assertEquals("Start result is 0 : ", firstResultInt, firstResult);
		assertEquals("End result is 10 : ", maxResultsInt, maxResults);
		assertEquals("There are 11 results : ", Integer.toString(resultsSizeInt), total);
		assertEquals("There are 11 results : ", resultsSizeInt, results.size());
	}

}