package ikube.web.admin;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Couck
 * @since 17.09.2011
 * @version 01.00
 */
public class IndexController extends BaseController {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewUrl = getViewUri(request);
		ModelAndView modelAndView = new ModelAndView(viewUrl);
		String indexName = request.getParameter(IConstants.INDEX_NAME);
		@SuppressWarnings("rawtypes")
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext<?> indexContext : indexContexts.values()) {
			if (indexName.equals(indexContext.getName())) {
				modelAndView.addObject(IConstants.INDEX_CONTEXT, indexContext);
				break;
			}
		}
		return modelAndView;
	}

}