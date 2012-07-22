package ikube.web.admin;

import ikube.IConstants;
import ikube.model.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * This is the search controller that will search all the indexes, in all the fields for the search strings. It will then merge the results
 * and sort them according to the highest score. Then the sub set of results are taken for the top ranking results. Meaning that some
 * indexes will not be represented in the results at all of course.
 * 
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public class SearchController extends SearchBaseController {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewUrl = getViewUri(request);
		ModelAndView modelAndView = new ModelAndView(viewUrl);
		Server server = clusterManager.getServer();

		// Get all the search strings from the request, we'll search all the indexes, all the fields, all strings
		Set<String> searchStrings = getSearchStrings(request);

		String[] indexNames = monitorService.getIndexNames();

		// Search all the indexes and merge the results
		int total = 0;
		long duration = 0;
		String corrections = null;
		List<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
		String[] searchStringsArray = searchStrings.toArray(new String[searchStrings.size()]);

		// Search all the indexes
		for (String indexName : indexNames) {
			ArrayList<HashMap<String, String>> indexResults = doSearch(request, modelAndView, indexName, searchStringsArray);
			if (indexResults != null) {
				HashMap<String, String> statistics = indexResults.get(indexResults.size() - 1);
				if (isNumeric(statistics.get(IConstants.TOTAL))) {
					total += Integer.parseInt(statistics.get(IConstants.TOTAL));
				}
				if (isNumeric(statistics.get(IConstants.DURATION))) {
					duration += Long.parseLong(statistics.get(IConstants.DURATION));
				}
				corrections = statistics.get(IConstants.CORRECTIONS);
				results.addAll(indexResults);
			}
			// Remove the statistics map from the end
			if (results.size() > 0) {
				results.remove(results.size() - 1);
			}
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

		int firstResult = getParameter(IConstants.FIRST_RESULT, FIRST_RESULT, request);
		int maxResults = getParameter(IConstants.MAX_RESULTS, MAX_RESULTS, request);
		// Now just take the top results, i.e. the max results that are defined by the user
		int subLength = results.size() < maxResults ? results.size() : maxResults;
		List<HashMap<String, String>> subResults = results.subList(0, subLength);
		results = new ArrayList<HashMap<String, String>>();
		results.addAll(subResults);

		// Put everything required in the model for the ui
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

	@SuppressWarnings("unchecked")
	protected Set<String> getSearchStrings(HttpServletRequest request) {
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
		return searchStrings;
	}

}