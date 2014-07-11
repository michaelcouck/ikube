package ikube.web.admin;

import ikube.IConstants;
import ikube.toolkit.PropertyConfigurer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * This controller will just put the properties in the model for display.
 * 
 * @author Michael Couck
 * @since 10.12.2011
 * @version 01.00
 */
@Controller
public class PropertiesController {

	@Autowired
	private PropertyConfigurer propertyConfigurer;

	/**
	 * {@inheritDoc}
	 */
	@RequestMapping(value = "/admin/properties.html", method = RequestMethod.GET)
	public ModelAndView terminate(@RequestParam(required = false, value = "targetView") String targetView, ModelAndView modelAndView)
			throws Exception {
		modelAndView.setViewName(targetView);
		modelAndView.addObject(IConstants.PROPERTIES, propertyConfigurer);
		return modelAndView;
	}

}