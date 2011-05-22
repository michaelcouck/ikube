package ikube.web.admin;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.Server;
import ikube.service.IMonitoringService;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.GeneralUtilities;
import ikube.toolkit.SerializationUtilities;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public class SearchController extends BaseController {

	private static final Logger LOGGER = Logger.getLogger(SearchController.class);
	private static final int MAX_RESULTS = 10;

	@Override
	@SuppressWarnings("unchecked")
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewUrl = getViewUri(request);
		ModelAndView modelAndView = new ModelAndView(viewUrl);

		String address = request.getParameter(IConstants.ADDRESS);
		String indexName = request.getParameter(IConstants.INDEX_NAME);

		List<Server> servers = ApplicationContextManager.getBean(IClusterManager.class).getServers();
		Server server = GeneralUtilities.findObject(Server.class, servers, IConstants.ADDRESS, address);

		modelAndView.addObject(IConstants.SERVER, server);

		String[] fieldNames = ApplicationContextManager.getBean(IMonitoringService.class).getIndexFieldNames(indexName);
		modelAndView.addObject(IConstants.SEARCH_FIELDS, fieldNames);

		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			LOGGER.debug("Parameter : " + parameterName + ", " + request.getParameter(parameterName));
		}

		List<String> searchFields = new ArrayList<String>();
		List<String> searchStrings = new ArrayList<String>();
		Map<String, String[]> parameterMap = request.getParameterMap();
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
		if (searchFields.size() > 0) {
			List<String> webServiceUrls = server.getWebServiceUrls();
			for (String webServiceUrl : webServiceUrls) {
				if (webServiceUrl.contains(ISearcherWebService.class.getSimpleName())) {
					ISearcherWebService searcherWebService = ServiceLocator.getService(ISearcherWebService.class, webServiceUrl,
							ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE);
					// LOGGER.error("Searcher web service : " + searcherWebService);
					String xml = searcherWebService.searchMulti(indexName, searchStrings.toArray(new String[searchStrings.size()]),
							searchFields.toArray(new String[searchFields.size()]), Boolean.TRUE, 0, MAX_RESULTS);

					List<Map<String, String>> results = (List<Map<String, String>>) SerializationUtilities.deserialize(xml);
					// LOGGER.error("Results : " + results);

					Map<String, String> statistics = results.get(results.size() - 1);
					String stringTotal = statistics.get(IConstants.TOTAL);
					String stringDuration = statistics.get(IConstants.DURATION);

					modelAndView.addObject(IConstants.TOTAL, stringTotal != null ? Integer.parseInt(stringTotal) : 0);
					modelAndView.addObject(IConstants.DURATION, stringDuration != null ? Integer.parseInt(stringDuration) : 0);

					results.remove(statistics);

					modelAndView.addObject(IConstants.RESULTS, results);
					modelAndView.addObject(IConstants.SEARCH_STRINGS, searchStrings.toString());
					// TODO For now we can still put this in the session but this will
					// only be used in the search tag so it can be removed when everything
					// is working just with the model
					request.getSession().setAttribute(IConstants.RESULTS, results);
					break;
				}
			}
		}

		return modelAndView;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return handleRequest(request, response);
	}

}
