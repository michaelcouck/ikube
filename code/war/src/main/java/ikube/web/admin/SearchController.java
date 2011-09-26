package ikube.web.admin;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.Server;
import ikube.service.IMonitorWebService;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.SerializationUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public class SearchController extends BaseController {

	/** These are the default values for first and max results. */
	private static final int	FIRST_RESULT	= 0;
	private static final int	MAX_RESULTS		= 10;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewUrl = getViewUri(request);
		ModelAndView modelAndView = new ModelAndView(viewUrl);
		Server server = ApplicationContextManager.getBean(IClusterManager.class).getServer();

		// Get all the search strings from the request, we'll search all the indexes, all the fields, all strings
		Set<String> searchStrings = new TreeSet<String>();
		Map<String, String[]> parameterMap = request.getParameterMap();
		for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
			// Check that the field is a search string field
			if (!entry.getKey().startsWith("search")) {
				continue;
			}
			String[] fieldValues = entry.getValue();
			// Check that there is a value in the fields from the request
			if (fieldValues == null || fieldValues.length == 0) {
				// Don't want to search for empty strings, not useful
				continue;
			}
			for (String fieldValue : fieldValues) {
				if (!StringUtils.hasLength(fieldValue)) {
					continue;
				}
				searchStrings.add(fieldValue);
			}
		}

		IMonitorWebService monitorWebService = ApplicationContextManager.getBean(IMonitorWebService.class);
		ISearcherWebService searcherWebService = ServiceLocator.getService(ISearcherWebService.class, server.getSearchWebServiceUrl(),
				ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);

		int firstResult = getParameter(IConstants.FIRST_RESULT, FIRST_RESULT, request);
		int maxResults = getParameter(IConstants.MAX_RESULTS, MAX_RESULTS, request);

		String[] indexNames = monitorWebService.getIndexNames();

		// Search all the indexes and merge the results
		int total = 0;
		long duration = 0;
		String corrections = null;
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		String[] searchStringsArray = searchStrings.toArray(new String[searchStrings.size()]);
		for (String indexName : indexNames) {
			String xml = searcherWebService.searchMultiAll(indexName, searchStringsArray, Boolean.TRUE, firstResult, maxResults);
			List<Map<String, String>> indexResults = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
			Map<String, String> statistics = indexResults.get(indexResults.size() - 1);
			if (isNumeric(statistics.get(IConstants.TOTAL))) {
				total += Integer.parseInt(statistics.get(IConstants.TOTAL));
			}
			if (isNumeric(statistics.get(IConstants.DURATION))) {
				duration += Long.parseLong(statistics.get(IConstants.DURATION));
			}
			corrections = statistics.get(IConstants.CORRECTIONS);
			indexResults.remove(statistics);
			results.addAll(indexResults);
		}

		// Sort the results according to the score. This will essentially merge the results and
		// the front end will then display the top maximum results regardless of the score. This
		// does mean that some indexes will never have any results of course
		Collections.sort(results, new Comparator<Map<String, String>>() {
			@Override
			public int compare(Map<String, String> o1, Map<String, String> o2) {
				Double s1 = Double.parseDouble(o1.get(IConstants.SCORE));
				Double s2 = Double.parseDouble(o2.get(IConstants.SCORE));
				return s1.compareTo(s2);
			}
		});

		// Now just take the top results, i.e. the max results that are defined by the user
		results = results.subList(0, results.size() < maxResults ? results.size() : maxResults);

		modelAndView.addObject(IConstants.TOTAL, total);
		modelAndView.addObject(IConstants.DURATION, duration);
		modelAndView.addObject(IConstants.RESULTS, results);
		modelAndView.addObject(IConstants.CORRECTIONS, corrections);

		String searchString = searchStrings.toString();
		// Strictly speaking this is not necessary because the searchers will clean the strings
		searchString = org.apache.commons.lang.StringUtils.strip(searchString, IConstants.STRIP_CHARACTERS);
		modelAndView.addObject(IConstants.SEARCH_STRINGS, searchString);
		String targetSearchUrl = getParameter(IConstants.TARGET_SEARCH_URL, "/results.html", request);
		modelAndView.addObject(IConstants.TARGET_SEARCH_URL, targetSearchUrl);

		modelAndView.addObject(IConstants.FIRST_RESULT, firstResult);
		modelAndView.addObject(IConstants.MAX_RESULTS, maxResults);

		modelAndView.addObject(IConstants.SERVER, server);
		return modelAndView;
	}

	private boolean isNumeric(String string) {
		if (string == null) {
			return Boolean.FALSE;
		}
		char[] chars = string.toCharArray();
		for (char c : chars) {
			if (c == '.') {
				continue;
			}
			if (Character.isDigit(c)) {
				continue;
			}
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	private String getParameter(String name, String defaultValue, HttpServletRequest request) {
		String parameter = request.getParameter(name);
		if (parameter != null && !"".equals(parameter)) {
			return parameter;
		}
		return defaultValue;
	}

	private int getParameter(String name, int defaultValue, HttpServletRequest request) {
		String parameter = request.getParameter(name);
		if (parameter != null && !"".equals(parameter) && org.apache.commons.lang.StringUtils.isNumeric(parameter)) {
			return Integer.parseInt(parameter);
		}
		return defaultValue;
	}

}