package ikube.web.admin;

import ikube.IConstants;
import ikube.model.Server;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
@Controller
public class GeoSearchController extends SearchBaseController {

	@RequestMapping(value = "/admin/geosearch.html", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView search(@RequestParam(required = false, value = "indexName") String indexName,
			@RequestParam(required = false, value = "searchStrings") String searchStrings,
			@RequestParam(required = false, value = "latitude") Double latitude,
			@RequestParam(required = false, value = "longitude") Double longitude,
			@RequestParam(required = false, value = "distance") Integer distance,
			@RequestParam(required = false, value = "firstResult") Integer firstResult,
			@RequestParam(required = false, value = "maxResults") Integer maxResults,
			@RequestParam(required = false, value = "targetSearchUrl") String targetSearchUrl, ModelAndView model) throws Exception {

		Server server = clusterManager.getServer();
		model.setViewName("/admin/geosearch");

		model.addObject(IConstants.LONGITUDE, longitude);
		model.addObject(IConstants.LATITUDE, latitude);
		model.addObject(IConstants.DISTANCE, distance);

		// TODO This should go in a validation framework
		if (indexName != null && searchStrings != null && latitude != null && longitude != null && distance != null && firstResult != null
				&& maxResults != null) {
			ArrayList<HashMap<String, String>> results = searcherWebService.searchMultiSpacialAll(indexName,
					new String[] { searchStrings }, Boolean.TRUE, firstResult, maxResults, distance, latitude, longitude);

			HashMap<String, String> statistics = results.get(results.size() - 1);
			String total = statistics.get(IConstants.TOTAL);
			String duration = statistics.get(IConstants.DURATION);
			String corrections = statistics.get(IConstants.CORRECTIONS);

			model.addObject(IConstants.INDEX_NAME, indexName);
			model.addObject(IConstants.TOTAL, total);
			model.addObject(IConstants.DURATION, duration);
			model.addObject(IConstants.RESULTS, results);
			model.addObject(IConstants.CORRECTIONS, corrections);
		}

		model.addObject(IConstants.SEARCH_STRINGS, searchStrings);
		model.addObject(IConstants.TARGET_SEARCH_URL, targetSearchUrl);

		model.addObject(IConstants.FIRST_RESULT, firstResult);
		model.addObject(IConstants.MAX_RESULTS, maxResults);

		model.addObject(IConstants.SERVER, server);
		return model;
	}

}