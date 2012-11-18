package ikube.web.admin;

import ikube.IConstants;
import ikube.service.ISearcherService;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
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
	protected ISearcherService searcherService;

	protected ArrayList<HashMap<String, String>> doSearch(HttpServletRequest request, ModelAndView modelAndView, String indexName,
			String... searchStrings) {
		int firstResult = getParameter(IConstants.FIRST_RESULT, FIRST_RESULT, request);
		int maxResults = getParameter(IConstants.MAX_RESULTS, MAX_RESULTS, request);

		String latitude = getParameter(IConstants.LATITUDE, null, request);
		String longitude = getParameter(IConstants.LONGITUDE, null, request);
		String distance = getParameter(IConstants.DISTANCE, null, request);

		ArrayList<HashMap<String, String>> results = null;
		if (searchStrings != null && searchStrings.length > 0) {
			boolean searchStringsEmpty = Boolean.FALSE;
			for (String searchString : searchStrings) {
				if (StringUtils.isEmpty(searchString)) {
					searchStringsEmpty = Boolean.TRUE;
					break;
				}
			}
			if (!searchStringsEmpty) {
				if (isNumeric(latitude) && isNumeric(longitude) && isNumeric(distance)) {
					// Do the geospatial search
					modelAndView.addObject(IConstants.LONGITUDE, longitude);
					modelAndView.addObject(IConstants.LATITUDE, latitude);
					modelAndView.addObject(IConstants.DISTANCE, distance);
					results = searcherService.searchMultiSpacialAll(indexName, searchStrings, Boolean.TRUE, firstResult, maxResults,
							Integer.parseInt(distance), Double.parseDouble(latitude), Double.parseDouble(longitude));
				} else {
					// Normal search with all the fields
					results = searcherService.searchMultiAll(indexName, searchStrings, Boolean.TRUE, firstResult, maxResults);
				}
			}
		}

		return results;
	}

}