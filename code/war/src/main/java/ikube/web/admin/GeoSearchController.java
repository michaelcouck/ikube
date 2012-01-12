package ikube.web.admin;

import ikube.IConstants;
import ikube.model.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
@Controller
public class GeoSearchController extends SearchBaseController {

	/**
	 * {@inheritDoc}
	 */
	@RequestMapping(value = "/admin/geosearch.html", method = RequestMethod.GET)
	public String terminate(@RequestParam(required = true, value = "targetView") String targetView,
			@RequestParam(required = true, value = "indexName") String indexName,
			@RequestParam(required = true, value = "searchStrings") String searchStrings,
			@RequestParam(required = true, value = "latitude") String latitude,
			@RequestParam(required = true, value = "longitude") String longitude,
			@RequestParam(required = true, value = "distance") String distance, Model model) throws Exception {

		Server server = clusterManager.getServer();

		// Search all the indexes and merge the results
		int total = 0;
		long duration = 0;
		String corrections = null;
		List<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
		String[] searchStringsArray = { searchStrings };

		ArrayList<HashMap<String, String>> indexResults = null; // doSearch(null, model, indexName, searchStringsArray);
		HashMap<String, String> statistics = indexResults.get(indexResults.size() - 1);
		if (isNumeric(statistics.get(IConstants.TOTAL))) {
			total += Integer.parseInt(statistics.get(IConstants.TOTAL));
		}
		if (isNumeric(statistics.get(IConstants.DURATION))) {
			duration += Long.parseLong(statistics.get(IConstants.DURATION));
		}
		corrections = statistics.get(IConstants.CORRECTIONS);
		results.addAll(indexResults);
		// Remove the statistics map from the end
		results.remove(results.size() - 1);

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

		int firstResult = getParameter(IConstants.FIRST_RESULT, FIRST_RESULT, null);
		int maxResults = getParameter(IConstants.MAX_RESULTS, MAX_RESULTS, null);
		// Now just take the top results, i.e. the max results that are defined by the user
		int subLength = results.size() < maxResults ? results.size() : maxResults;
		List<HashMap<String, String>> subResults = results.subList(0, subLength);
		results = new ArrayList<HashMap<String, String>>();
		results.addAll(subResults);

		model.addAttribute(IConstants.TOTAL, total);
		model.addAttribute(IConstants.DURATION, duration);
		model.addAttribute(IConstants.RESULTS, results);
		model.addAttribute(IConstants.CORRECTIONS, corrections);

		String searchString = searchStrings.toString();
		// Strictly speaking this is not necessary because the searchers will clean the strings
		searchString = org.apache.commons.lang.StringUtils.strip(searchString, IConstants.STRIP_CHARACTERS);
		model.addAttribute(IConstants.SEARCH_STRINGS, searchString);
		String targetSearchUrl = getParameter(IConstants.TARGET_SEARCH_URL, "/results.html", null);
		model.addAttribute(IConstants.TARGET_SEARCH_URL, targetSearchUrl);

		model.addAttribute(IConstants.FIRST_RESULT, firstResult);
		model.addAttribute(IConstants.MAX_RESULTS, maxResults);

		model.addAttribute(IConstants.SERVER, server);
		return targetView;
	}

}