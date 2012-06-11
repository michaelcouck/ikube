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

			model.addObject(IConstants.TOTAL, total);
			model.addObject(IConstants.DURATION, duration);
			model.addObject(IConstants.RESULTS, results);
			model.addObject(IConstants.CORRECTIONS, corrections);
		}

		model.addObject(IConstants.INDEX_NAME, indexName);
		model.addObject(IConstants.SEARCH_STRINGS, searchStrings);
		model.addObject(IConstants.TARGET_SEARCH_URL, targetSearchUrl);

		model.addObject(IConstants.FIRST_RESULT, firstResult);
		model.addObject(IConstants.MAX_RESULTS, maxResults);

		model.addObject(IConstants.SERVER, server);
		return model;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/admin/georoute.html", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView route(@RequestParam(required = false, value = "indexName") String indexName,
			@RequestParam(required = false, value = "searchStrings") String searchStrings,
			@RequestParam(required = false, value = "latitude") Double latitude,
			@RequestParam(required = false, value = "longitude") Double longitude,
			@RequestParam(required = false, value = "distance") Integer distance,
			@RequestParam(required = false, value = "firstResult") Integer firstResult,
			@RequestParam(required = false, value = "maxResults") Integer maxResults,
			@RequestParam(required = false, value = "targetSearchUrl") String targetSearchUrl, ModelAndView model) throws Exception {
		search(indexName, searchStrings, latitude, longitude, distance, firstResult, maxResults, targetSearchUrl, model);
		// Find the first result
		ArrayList<HashMap<String, String>> results = (ArrayList<HashMap<String, String>>) model.getModel().get(IConstants.RESULTS);
		// Remove the statistics result from the end of the results
		HashMap<String, String> statistics = results.remove(results.size() - 1);
		ArrayList<HashMap<String, String>> resultsRouted = new ArrayList<HashMap<String, String>>();
		model.addObject(IConstants.RESULTS_ROUTED, resultsRouted);
		if (results.size() > 1) {
			HashMap<String, String> topResult = results.get(0);
			resultsRouted.add(topResult);
			do {
				// Recursively find the closest result to the top result and add it to the sorted array
				HashMap<String, String> bestResult = null;
				double bestDistance = Long.MAX_VALUE;
				for (HashMap<String, String> nextResult : results) {
					if (topResult == nextResult || resultsRouted.contains(nextResult)) {
						continue;
					}
					double lat1 = Double.parseDouble(topResult.get(IConstants.LATITUDE));
					double lon1 = Double.parseDouble(topResult.get(IConstants.LONGITUDE));
					double lat2 = Double.parseDouble(nextResult.get(IConstants.LATITUDE));
					double lon2 = Double.parseDouble(nextResult.get(IConstants.LONGITUDE));
					double nextDistance = distance(lat1, lon1, lat2, lon2, 'K');
					if (nextDistance < bestDistance) {
						bestDistance = nextDistance;
						bestResult = nextResult;
					}
				}
				resultsRouted.add(bestResult);
				topResult = bestResult;
			} while (resultsRouted.size() < results.size());
		}
		results.add(statistics);
		return model;
	}

	/* :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: : */
	/* :: This routine calculates the distance between two points (given the : */
	/* :: latitude/longitude of those points). It is being used to calculate : */
	/* :: the distance between two ZIP Codes or Postal Codes using our : */
	/* :: ZIPCodeWorld(TM) and PostalCodeWorld(TM) products. : */
	/* :: : */
	/* :: Definitions: : */
	/* :: South latitudes are negative, east longitudes are positive : */
	/* :: : */
	/* :: Passed to function: : */
	/* :: lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees) : */
	/* :: lat2, lon2 = Latitude and Longitude of point 2 (in decimal degrees) : */
	/* :: unit = the unit you desire for results : */
	/* :: where: 'M' is statute miles : */
	/* :: 'K' is kilometers (default) : */
	/* :: 'N' is nautical miles : */
	/* :: United States ZIP Code/ Canadian Postal Code databases with latitude & : */
	/* :: longitude are available at http://www.zipcodeworld.com : */
	/* :: : */
	/* :: For enquiries, please contact sales@zipcodeworld.com : */
	/* :: : */
	/* :: Official Web site: http://www.zipcodeworld.com : */
	/* :: : */
	/* :: Hexa Software Development Center © All Rights Reserved 2004 : */
	/* :: : */
	/* :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */

	private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == 'K') {
			dist = dist * 1.609344;
		} else if (unit == 'N') {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts decimal degrees to radians : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts radians to decimal degrees : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	// system.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "M") + " Miles\n");
	// system.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "K") + " Kilometers\n");
	// system.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "N") + " Nautical Miles\n");

}