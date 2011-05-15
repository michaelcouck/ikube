package ikube.web.admin;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public abstract class BaseController extends AbstractController {

	protected String getViewUri(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String context = request.getContextPath();
		String uriSansSuffix = StringUtils.stripFilenameExtension(uri);
		String uriSansContext = StringUtils.replace(uriSansSuffix, context, "");
		return uriSansContext;
	}

}
