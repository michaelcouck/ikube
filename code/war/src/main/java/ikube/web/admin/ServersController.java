package ikube.web.admin;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

	public ServersController() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewUrl = getViewUri(request);
		ModelAndView modelAndView = new ModelAndView(viewUrl);
		// Get the servers and sort them
		List<Server> servers = new ArrayList<Server>(clusterManager.getServers().values());

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
		modelAndView.addObject(IConstants.ACTION, server.getActions());
		// modelAndView.addObject(IConstants.INDEXING_EXECUTIONS, server.getIndexingExecutions());
		// modelAndView.addObject(IConstants.SEARCHING_EXECUTIONS, server.getSearchingExecutions());

		@SuppressWarnings("rawtypes")
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		modelAndView.addObject(IConstants.INDEX_CONTEXTS, indexContexts.values());

		for (IndexContext<?> indexContext : indexContexts.values()) {
			String indexName = indexContext.getIndexName();
			long indexSize = monitorWebService.getIndexSize(indexName);
			long numDocs = monitorWebService.getIndexDocuments(indexName);
			indexContext.setIndexSize(indexSize);
			indexContext.setNumDocs(numDocs);
		}

		return modelAndView;
	}

}