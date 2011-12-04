package ikube.web.admin;

import ikube.IConstants;
import ikube.service.ISearcherWebService;
import ikube.toolkit.SerializationUtilities;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Couck
 * @since 04.12.2011
 * @version 01.00
 */
public abstract class SearchBaseController extends BaseController {

	/** These are the default values for first and max results. */
	protected static final int FIRST_RESULT = 0;
	protected static final int MAX_RESULTS = 10;

	@Autowired
	protected ISearcherWebService searcherWebService;

	@SuppressWarnings("unchecked")
	protected List<Map<String, String>> doSearch(HttpServletRequest request, ModelAndView modelAndView, String indexName,
			String... searchStrings) {
		int firstResult = getParameter(IConstants.FIRST_RESULT, FIRST_RESULT, request);
		int maxResults = getParameter(IConstants.MAX_RESULTS, MAX_RESULTS, request);

		String latitude = getParameter(IConstants.LATITUDE, null, request);
		String longitude = getParameter(IConstants.LONGITUDE, null, request);
		String distance = getParameter(IConstants.DISTANCE, null, request);

		String xml = null;
		if (isNumeric(latitude) && isNumeric(longitude) && isNumeric(distance)) {
			// Do the geospatial search
			modelAndView.addObject(IConstants.LONGITUDE, longitude);
			modelAndView.addObject(IConstants.LATITUDE, latitude);
			modelAndView.addObject(IConstants.DISTANCE, distance);
			searcherWebService.searchSpacialMultiAll(indexName, searchStrings, Boolean.TRUE, firstResult, maxResults,
					Integer.parseInt(distance), Double.parseDouble(latitude), Double.parseDouble(longitude));
		} else {
			// Normal search with all the fields
			xml = searcherWebService.searchMultiAll(indexName, searchStrings, Boolean.TRUE, firstResult, maxResults);
		}

		if (xml != null) {
			return (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
		}

		return null;
	}

}
