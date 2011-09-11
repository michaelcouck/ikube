package ikube.web.admin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.IConstants;
import ikube.mock.ApplicationContextManagerWebMock;
import ikube.mock.ServiceLocatorWebMock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mockit.Mocked;
import mockit.Mockit;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

public class SearchControllerTest {

	private static final Logger	LOGGER		= Logger.getLogger(SearchControllerTest.class);

	/** Class under test. */
	@Mocked(inverse = false, methods = { "getViewUri" })
	private SearchController	searchController;

	private HttpServletRequest	request		= mock(HttpServletRequest.class);
	private HttpServletResponse	response	= mock(HttpServletResponse.class);

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerWebMock.class, ServiceLocatorWebMock.class);
		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		parameterMap.put(IConstants.SEARCH_STRINGS, new String[] { IConstants.IKUBE });
		when(request.getParameterMap()).thenReturn(parameterMap);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void handleRequest() throws Exception {
		// HttpServletRequest request, HttpServletResponse response
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
		assertEquals("Start result is 10 : ", 10, maxResults);
		assertEquals("There are 11 * 3 results : ", 33, total);
		assertEquals("There are 10 * 3 results : ", 30, results.size());
	}

}
