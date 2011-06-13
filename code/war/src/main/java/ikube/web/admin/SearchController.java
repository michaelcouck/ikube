package ikube.web.admin;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.Server;
import ikube.service.IMonitorWebService;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.SerializationUtilities;

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
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public class SearchController extends BaseController {

	private static final int MAX_RESULTS = 10;
	private static final int FIRST_RESULT = 0;

	@Override
	@SuppressWarnings("unchecked")
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewUrl = getViewUri(request);
		ModelAndView modelAndView = new ModelAndView(viewUrl);
		String indexName = request.getParameter(IConstants.INDEX_NAME);
		Server server = ApplicationContextManager.getBean(IClusterManager.class).getServer();

		IMonitorWebService monitorWebService = ApplicationContextManager.getBean(IMonitorWebService.class);
		String[] indexableNames = monitorWebService.getIndexableNames(indexName);
		Map<String, String[]> indexables = new HashMap<String, String[]>();
		Set<String> searchFields = new TreeSet<String>();
		Set<String> searchStrings = new TreeSet<String>();
		Map<String, String[]> parameterMap = request.getParameterMap();
		for (String indexableName : indexableNames) {
			String[] fieldNames = monitorWebService.getIndexableFieldNames(indexableName);
			indexables.put(indexableName, fieldNames);
			for (String fieldName : fieldNames) {
				if (parameterMap.containsKey(fieldName)) {
					String[] fieldValues = parameterMap.get(fieldName);
					// Check that there is a value in the fields from the request
					if (fieldValues == null || fieldValues.length == 0 || !StringUtils.hasLength(fieldValues[0].trim())) {
						// Don't want to search for empty strings, not useful
						continue;
					}
					searchFields.add(fieldName);
					searchStrings.add(fieldValues[0]);
					modelAndView.addObject(fieldName, fieldValues[0]);
				}
			}
		}

		int firstResult = FIRST_RESULT;
		int maxResults = MAX_RESULTS;
		if (searchFields.size() > 0) {
			String parameter = request.getParameter(IConstants.FIRST_RESULT);
			try {
				if (parameter != null && !"".equals(parameter) && org.apache.commons.lang.StringUtils.isNumeric(parameter)) {
					firstResult = Integer.parseInt(parameter);
				}
			} catch (Exception e) {
				logger.warn("The first results parameter : " + parameter);
			}
			
			parameter = request.getParameter(IConstants.MAX_RESULTS);
			try {
				if (parameter != null && !"".equals(parameter) && org.apache.commons.lang.StringUtils.isNumeric(parameter)) {
					maxResults = Integer.parseInt(parameter);
				}
			} catch (Exception e) {
				logger.warn("The max results parameter : " + parameter);
			}
			
			
			ISearcherWebService searcherWebService = ServiceLocator.getService(ISearcherWebService.class, server.getSearchWebServiceUrl(),
					ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);
			// LOGGER.error("Searcher web service : " + searcherWebService);
			String xml = searcherWebService.searchMulti(indexName, searchStrings.toArray(new String[searchStrings.size()]),
					searchFields.toArray(new String[searchFields.size()]), Boolean.TRUE, firstResult, maxResults);

			List<Map<String, String>> results = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
			// LOGGER.error("Results : " + results);

			Map<String, String> statistics = results.get(results.size() - 1);
			String stringTotal = statistics.get(IConstants.TOTAL);
			String stringDuration = statistics.get(IConstants.DURATION);

			modelAndView.addObject(IConstants.TOTAL, stringTotal != null ? Integer.parseInt(stringTotal) : 0);
			modelAndView.addObject(IConstants.DURATION, stringDuration != null ? Integer.parseInt(stringDuration) : 0);

			results.remove(statistics);

			modelAndView.addObject(IConstants.RESULTS, results);
			// TODO For now we can still put this in the session but this will
			// only be used in the search tag so it can be removed when everything
			// is working just with the model
			request.getSession().setAttribute(IConstants.RESULTS, results);
		}

		String searchString = searchStrings.toString();
		// Strictly speaking this is not necessary because the searchers will clean the strings
		searchString = org.apache.commons.lang.StringUtils.strip(searchString, "[]{},");

		modelAndView.addObject(IConstants.FIRST_RESULT, firstResult);
		modelAndView.addObject(IConstants.MAX_RESULTS, maxResults);
		
		modelAndView.addObject(IConstants.SEARCH_STRINGS, searchString);
		modelAndView.addObject(IConstants.INDEX_NAME, indexName);
		modelAndView.addObject(IConstants.SERVER, server);
		modelAndView.addObject(IConstants.INDEXABLES, indexables);
		return modelAndView;
	}

}