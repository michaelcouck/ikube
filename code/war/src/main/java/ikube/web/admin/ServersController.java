package ikube.web.admin;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public class ServersController extends BaseController {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(ServersController.class);

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewUrl = getViewUri(request);
		// JOptionPane.showInputDialog("Hello controller : " + request.getRequestURI() + ", " + viewUrl);
		ModelAndView modelAndView = new ModelAndView(viewUrl);
		List<Server> servers = ApplicationContextManager.getBean(IClusterManager.class).getServers();
		modelAndView.addObject(IConstants.SERVERS, servers);
		return modelAndView;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return handleRequest(request, response);
	}

}
