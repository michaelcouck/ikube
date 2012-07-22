package ikube.web.admin;

import ikube.IConstants;
import ikube.model.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * This search controller will search a particular index and put the results in the model.
 * 
 * @author Michael Couck
 * @since 04.12.11
 * @version 01.00
 */
public class SearchIndexController extends SearchBaseController {

	protected Logger logger = Logger.getLogger(this.getClass());

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewUrl = getViewUri(request);
		ModelAndView modelAndView = new ModelAndView(viewUrl);
		Server server = clusterManager.getServer();

		ArrayList<HashMap<String, String>> results = null;
		HashMap<String, String> fieldNamesAndValues = new HashMap<String, String>();
		int firstResult = getParameter(IConstants.FIRST_RESULT, FIRST_RESULT, request);
		int maxResults = getParameter(IConstants.MAX_RESULTS, MAX_RESULTS, request);
		// If the 'searchStrings' is in the request then this is a search on all fields
		String indexName = getParameter(IConstants.INDEX_NAME, null, request);
		String searchString = getParameter(IConstants.SEARCH_STRINGS, null, request);
		char delimiter = ',';
		String[] searchStrings = null;
		if (searchString != null && searchString.indexOf(delimiter) > -1) {
			searchStrings = StringUtils.split(searchString, ",");
		} else {
			searchStrings = new String[] { searchString };
		}
		String[] indexFieldNames = monitorService.getIndexFieldNames(indexName);
		for (String indexFieldName : indexFieldNames) {
			fieldNamesAndValues.put(indexFieldName, "");
		}
		if (StringUtils.hasLength(searchString)) {
			results = doSearch(request, modelAndView, indexName, searchStrings);
		} else {
			// Search all the fields individually
			List<String> searchFields = new ArrayList<String>();
			for (String indexFieldName : indexFieldNames) {
				String indexFieldValue = getParameter(indexFieldName, null, request);
				if (StringUtils.hasLength(indexFieldValue)) {
					searchFields.add(indexFieldName);
					fieldNamesAndValues.put(indexFieldName, indexFieldValue);
				}
			}
			String[] searchFieldsArray = searchFields.toArray(new String[searchFields.size()]);
			results = searcherService.searchMulti(indexName, searchStrings, searchFieldsArray, Boolean.TRUE, firstResult, maxResults);
		}

		Map<String, String> statistics = results.get(results.size() - 1);

		modelAndView.addObject(IConstants.RESULTS, results);
		modelAndView.addObject(IConstants.TOTAL, statistics.get(IConstants.TOTAL));
		modelAndView.addObject(IConstants.DURATION, statistics.get(IConstants.DURATION));
		modelAndView.addObject(IConstants.CORRECTIONS, statistics.get(IConstants.CORRECTIONS));
		modelAndView.addObject(IConstants.SEARCH_STRINGS, searchString);

		String targetSearchUrl = getParameter(IConstants.TARGET_SEARCH_URL, "/admin/search.html", request);
		modelAndView.addObject(IConstants.TARGET_SEARCH_URL, targetSearchUrl);
		modelAndView.addObject(IConstants.FIRST_RESULT, firstResult);
		modelAndView.addObject(IConstants.MAX_RESULTS, maxResults);
		modelAndView.addObject(IConstants.INDEX_NAME, indexName);
		modelAndView.addObject(IConstants.INDEX_FIELD_NAMES_AND_VALUES, fieldNamesAndValues);
		modelAndView.addObject(IConstants.SERVER, server);
		return modelAndView;
	}
	
}