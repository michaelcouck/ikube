package ikube.web.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * This is the base for the controllers. The method getViewUrl will strip the file name and the context from the request url so that the
 * mapping in the Spring MVC will forward to the correct page. The request url will be something like /ikube/results.html. The mapping to
 * the results page in Spring is /results.html. The search controller will then be called first, the parameters in the request will be used
 * to do the search against the index. The parameters will have the index name, the fields to search in the index and the search string(s).
 * The search controller will put the results in the response for the page. Then the context (/ikube) will be stripped from the url, and the
 * file extension(.html), the result of this stripping will be '/results'. As defined in the web-application-context.xml, the prefix
 * '/WEB-INF/jsp/' will be added to the view url, and the extension '.jsp'. This will then be handed off to the view-controller as defined
 * in the context configuration. The response will then be forwarded to this url.
 * 
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public abstract class BaseController extends AbstractController {

	/**
	 * TODO Document me.
	 * 
	 * @param request
	 * @return
	 */
	protected String getViewUri(HttpServletRequest request) {
		// Get the request url
		String uri = request.getRequestURI();
		String context = request.getContextPath();
		// Strip the context and the file extension
		String uriSansSuffix = StringUtils.stripFilenameExtension(uri);
		String uriSansContext = StringUtils.replace(uriSansSuffix, context, "");
		// Spring will forward to the correct page
		return uriSansContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return handleRequest(request, response);
	}

}
