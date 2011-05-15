package ikube.web.admin;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.service.IMonitoringService;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.GeneralUtilities;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public class ServerController extends BaseController {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(ServerController.class);

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

		String[] indexNames = ApplicationContextManager.getBean(IMonitoringService.class).getIndexNames();
		modelAndView.addObject(IConstants.INDEX_NAMES, indexNames);

		return modelAndView;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return handleRequest(request, response);
	}

}
