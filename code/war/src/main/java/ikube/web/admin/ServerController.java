package ikube.web.admin;

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

public class ServerController extends BaseController {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(ServerController.class);

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewUrl = getViewUri(request);
		ModelAndView modelAndView = new ModelAndView(viewUrl);

		String address = request.getParameter("address");
		List<Server> servers = ApplicationContextManager.getBean(IClusterManager.class).getServers();
		Server server = GeneralUtilities.findObject(Server.class, servers, "address", address);

		modelAndView.addObject("server", server);
		modelAndView.addObject("actions", server.getActions());
		modelAndView.addObject("webServiceUrls", server.getWebServiceUrls());

		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		modelAndView.addObject("indexContexts", indexContexts.values());

		String[] indexNames = ApplicationContextManager.getBean(IMonitoringService.class).getIndexNames();
		modelAndView.addObject("indexNames", indexNames);

		return modelAndView;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return handleRequest(request, response);
	}

}
