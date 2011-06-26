package ikube.web.admin;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.service.IMonitorWebService;
import ikube.toolkit.ApplicationContextManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * This controller will get all the servers and put them in the response to be made available to the front end.
 * 
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public class ServersController extends BaseController {

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewUrl = getViewUri(request);
		ModelAndView modelAndView = new ModelAndView(viewUrl);
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		// Get the servers and sort them
		List<Server> servers = clusterManager.getServers();
		Collections.sort(servers, new Comparator<Server>() {
			@Override
			public int compare(Server o1, Server o2) {
				return o1.getAddress().compareTo(o2.getAddress());
			}
		});
		modelAndView.addObject(IConstants.SERVERS, servers);

		// Put the server and other related stuff in the response
		Server server = clusterManager.getServer();
		modelAndView.addObject(IConstants.SERVER, server);
		modelAndView.addObject(IConstants.ACTION, server.getAction());
		modelAndView.addObject(IConstants.WEB_SERVICE_URLS, server.getWebServiceUrls());
		modelAndView.addObject(IConstants.INDEXING_EXECUTIONS, server.getIndexingExecutions());
		modelAndView.addObject(IConstants.SEARCHING_EXECUTIONS, server.getSearchingExecutions());

		@SuppressWarnings("rawtypes")
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		modelAndView.addObject(IConstants.INDEX_CONTEXTS, indexContexts.values());

		IMonitorWebService monitorWebService = ApplicationContextManager.getBean(IMonitorWebService.class);
		for (IndexContext<?> indexContext : indexContexts.values()) {
			String indexName = indexContext.getIndexName();
			long indexSize = monitorWebService.getIndexSize(indexName);
			long numDocs = monitorWebService.getIndexDocuments(indexName);
			indexContext.setIndexSize(indexSize);
			indexContext.setNumDocs(numDocs);
		}

		// TODO This can be removed
		String[] indexNames = monitorWebService.getIndexNames();
		modelAndView.addObject(IConstants.INDEX_NAMES, indexNames);

		for (String indexName : indexNames) {
			Map<String, Object> indexProperties = new HashMap<String, Object>();
			long indexSize = monitorWebService.getIndexSize(indexName);
			int numDocs = monitorWebService.getIndexDocuments(indexName);
			indexProperties.put(IConstants.INDEX_SIZE, indexSize);
			indexProperties.put(IConstants.INDEX_DOCUMENTS, numDocs);
			modelAndView.addObject(indexName, indexProperties);
		}

		return modelAndView;
	}

}