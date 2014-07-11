package ikube.web.admin;

import ikube.IConstants;
import ikube.model.Server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Couck
 * @since 22.05.2011
 * @version 01.00
 */
public class LoggingController extends BaseController {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewUrl = getViewUri(request);
		ModelAndView modelAndView = new ModelAndView(viewUrl);
		Server server = clusterManager.getServer();
		modelAndView.addObject(IConstants.SERVER, server);
		return modelAndView;
	}

}
