package ikube.web.admin;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.service.IMonitoringService;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.GeneralUtilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public class ServerController extends BaseController {

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewUrl = getViewUri(request);
		ModelAndView modelAndView = new ModelAndView(viewUrl);

		String address = request.getParameter(IConstants.ADDRESS);
		List<Server> servers = ApplicationContextManager.getBean(IClusterManager.class).getServers();
		Server server = GeneralUtilities.findObject(Server.class, servers, IConstants.ADDRESS, address);

		modelAndView.addObject(IConstants.SERVER, server);
		modelAndView.addObject(IConstants.ACTIONS, server.getActions());
		modelAndView.addObject(IConstants.WEB_SERVICE_URLS, server.getWebServiceUrls());

		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		modelAndView.addObject(IConstants.INDEX_CONTEXTS, indexContexts.values());

		IMonitoringService monitoringService = ApplicationContextManager.getBean(IMonitoringService.class);
		String[] indexNames = monitoringService.getIndexNames();
		modelAndView.addObject(IConstants.INDEX_NAMES, indexNames);

		for (String indexName : indexNames) {
			Map<String, Object> indexProperties = new HashMap<String, Object>();
			long indexSize = monitoringService.getIndexSize(indexName);
			int numDocs = monitoringService.getIndexDocuments(indexName);
			indexProperties.put(IConstants.INDEX_SIZE, indexSize);
			indexProperties.put(IConstants.INDEX_DOCUMENTS, numDocs);
			modelAndView.addObject(indexName, indexProperties);
		}

		modelAndView.addObject(IConstants.INDEXING_EXECUTIONS, server.getIndexingExecutions());
		modelAndView.addObject(IConstants.SEARCHING_EXECUTIONS, server.getSearchingExecutions());

		return modelAndView;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return handleRequest(request, response);
	}

}
