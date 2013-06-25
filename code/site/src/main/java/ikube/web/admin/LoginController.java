package ikube.web.admin;

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
public class LoginController {

	/**
	 * This method will be called by Spring security, as defined in the security configuration, and forward to the login page which is typically under the
	 * WEB-INF folder.
	 * 
	 * @param model the model for the application if there is one
	 * @return the login page view
	 */
	@RequestMapping(value = "/login.html", method = { RequestMethod.GET, RequestMethod.POST })
	public String get(final Model model) {
		return "/login";
	}

}