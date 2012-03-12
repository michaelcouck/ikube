package ikube.web.admin;

import ikube.model.IndexContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Couck
 * @since 05.03.2012
 * @version 01.00
 */
@Controller
public class IndexableController {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexableController.class);

	@ModelAttribute
	/**
	 * {@inheritDoc}
	 */
	@RequestMapping(value = "/admin/indexContext", method = RequestMethod.POST)
	public ModelAndView createIndexContext(@ModelAttribute final IndexContext<?> indexContext, final String targetView,
			final ModelAndView modelAndView) {
		LOGGER.info("Index context : " + indexContext);
		modelAndView.setViewName(targetView);
		return modelAndView;
	}

}