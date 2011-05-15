package ikube.web.admin;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.AbstractController;

public abstract class BaseController extends AbstractController {

	protected String getViewUri(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String context = request.getContextPath();
		String uriSansSuffix = StringUtils.stripFilenameExtension(uri);
		String uriSansContext = StringUtils.replace(uriSansSuffix, context, "");
		// String viewUri = StringUtils.strip(StringUtils.strip(uri, context), ".html");
		// JOptionPane.showMessageDialog(null, uri + ", " + context + ", " + uriSansContext);
		return uriSansContext;
	}

}
