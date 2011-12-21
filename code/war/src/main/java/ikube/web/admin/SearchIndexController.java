package ikube.web.admin;

import ikube.IConstants;
import ikube.model.Server;
import ikube.toolkit.SerializationUtilities;

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
	@SuppressWarnings("unchecked")
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewUrl = getViewUri(request);
		ModelAndView modelAndView = new ModelAndView(viewUrl);
		Server server = clusterManager.getServer();

		List<Map<String, String>> results = null;
		Map<String, String> fieldNamesAndValues = new HashMap<String, String>();
		int firstResult = getParameter(IConstants.FIRST_RESULT, FIRST_RESULT, request);
		int maxResults = getParameter(IConstants.MAX_RESULTS, MAX_RESULTS, request);
		// If the 'searchStrings' is in the request then this is a search on all fields
		String indexName = getParameter(IConstants.INDEX_NAME, null, request);
		String searchString = getParameter(IConstants.SEARCH_STRINGS, null, request);
		String[] indexFieldNames = monitorWebService.getIndexFieldNames(indexName);
		for (String indexFieldName : indexFieldNames) {
			fieldNamesAndValues.put(indexFieldName, "");
		}
		if (StringUtils.hasLength(searchString)) {
			results = doSearch(request, modelAndView, indexName, searchString);
		} else {
			// Search all the fields individually
			List<String> searchFields = new ArrayList<String>();
			List<String> searchStrings = new ArrayList<String>();
			for (String indexFieldName : indexFieldNames) {
				String indexFieldValue = getParameter(indexFieldName, null, request);
				if (StringUtils.hasLength(indexFieldValue)) {
					searchFields.add(indexFieldName);
					searchStrings.add(indexFieldValue);
					fieldNamesAndValues.put(indexFieldName, indexFieldValue);
				}
			}
			String xml = searcherWebService.searchMulti(indexName, searchStrings.toArray(new String[searchStrings.size()]),
					searchFields.toArray(new String[searchFields.size()]), Boolean.TRUE, firstResult, maxResults);
			results = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
		}

		Map<String, String> statistics = results.get(results.size() - 1);

		modelAndView.addObject(IConstants.RESULTS, results);
		modelAndView.addObject(IConstants.TOTAL, statistics.get(IConstants.TOTAL));
		modelAndView.addObject(IConstants.DURATION, statistics.get(IConstants.DURATION));
		modelAndView.addObject(IConstants.CORRECTIONS, statistics.get(IConstants.CORRECTIONS));
		modelAndView.addObject(IConstants.SEARCH_STRINGS, statistics.get(IConstants.SEARCH_STRINGS));

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