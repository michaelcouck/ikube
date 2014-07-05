package ikube.web.admin;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.ApplicationContextManager;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * This controller sets the properties for the index in the model for the user interface. Also extends the search controller because it will
 * also do searches on individual indexes.
 * 
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
		if (logger.isDebugEnabled()) {
			logger.debug("Parameters : " + request.getParameterMap());
		}
		IndexContext<?> indexContext = ApplicationContextManager.getBean(indexName);
		// Should never be null of course
		if (indexContext != null) {
			modelAndView.addObject(IConstants.INDEX_NAME, indexContext.getName());
			modelAndView.addObject(IConstants.INDEX_CONTEXT, indexContext);
			modelAndView.addObject(IConstants.GEOSPATIAL, isGeospatial(indexContext.getChildren()));
		}
		return modelAndView;
	}

	private boolean isGeospatial(List<Indexable<?>> indexables) {
		boolean isGeospatial = Boolean.FALSE;
		if (indexables != null) {
			for (Indexable<?> indexable : indexables) {
				isGeospatial = indexable.isAddress() | isGeospatial(indexable.getChildren());
			}
		}
		return isGeospatial;
	}

	protected String[] getIndexNames(HttpServletRequest request) {
		return new String[] { request.getParameter(IConstants.INDEX_NAME) };
	}

}