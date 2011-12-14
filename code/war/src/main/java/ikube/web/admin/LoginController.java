package ikube.web.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This controller just takes the request to the login page.
 * 
 * @author Michael Couck
 * @since 09.08.2011
 * @version 01.00
 */
@Controller
public class LoginController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

	/**
	 * This method will be called by Spring security, as defined in the security configuration, and forward to the login page which is
	 * typically under the WEB-INF folder.
	 * 
	 * @param model the model for the application if there is one
	 * @param request the request
	 * @param response the response
	 * @return the login page view
	 */
	@RequestMapping(value = "/admin/login.html", method = { RequestMethod.GET, RequestMethod.POST })
	public String get(Model model, HttpServletRequest request, HttpServletResponse response) {
		LOGGER.debug("Login page : {}", model);
		return "/admin/login";
	}

}