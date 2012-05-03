package ikube.web.admin;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.Index;
import ikube.model.Search;
import ikube.model.Server;
import ikube.toolkit.DatabaseUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * This action will go to the database and fetch a certain number of the class type specified in the parameter list. It acts as a pager
 * through various entities like the searches, that are stored in the database.
 * 
 * @author Michael Couck
 * @since 22.12.2011
 * @version 01.00
 */
@Controller
public class DatabaseController extends BaseController {

	/** Database access for fetching the entities that are stored in ikube. */
	@Autowired
	private IDataBase dataBase;

	/**
	 * {@inheritDoc}
	 */
	@RequestMapping(value = "/admin/database.html", method = RequestMethod.GET)
	public ModelAndView entities(@RequestParam(required = true, value = "targetView") String targetView,
			@RequestParam(required = true, value = "classType") String classType,
			@RequestParam(required = true, value = "sortFields") List<String> sortFields,
			@RequestParam(required = true, value = "directionOfSort") List<Boolean> directionOfSort,
			@RequestParam(required = true, value = "start") int start, @RequestParam(required = true, value = "end") int end,
			ModelAndView modelAndView) throws Exception {
		Class<?> klass = Class.forName(classType);
		Long total = dataBase.count(Search.class);
		List<?> list = dataBase.find(klass, sortFields.toArray(new String[sortFields.size()]),
				directionOfSort.toArray(new Boolean[directionOfSort.size()]), start, end);
		List<String> fieldNames = DatabaseUtilities.getFieldNames(klass, new ArrayList<String>());
		Server server = clusterManager.getServer();

		modelAndView.addObject(IConstants.TOTAL, total);
		modelAndView.addObject(IConstants.ENTITIES, list);
		modelAndView.addObject(IConstants.FIELD_NAMES, fieldNames);
		modelAndView.addObject(IConstants.SERVER, server);

		modelAndView.setViewName(targetView);
		return modelAndView;
	}

	/**
	 * {@inheritDoc}
	 */
	@RequestMapping(value = "/admin/database.html", method = RequestMethod.GET)
	public ModelAndView select(@RequestParam(required = true, value = "targetView") String targetView,
			@RequestParam(required = true, value = "class") final String klass,
			@RequestParam(required = true, value = "fieldsToFilterOn") final List<String> fieldsToFilterOn,
			@RequestParam(required = true, value = "valuesToFilterOn") final List<Object> valuesToFilterOn,
			@RequestParam(required = true, value = "firstResult") final int firstResult,
			@RequestParam(required = true, value = "maxResults") final int maxResults, final ModelAndView modelAndView) throws Exception {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.ACTION_NAME, Index.class.getSimpleName());
		Long total = dataBase.count(Class.forName(klass));
		List<?> actions = dataBase.findCriteria(Class.forName(klass), fieldsToFilterOn.toArray(new String[fieldsToFilterOn.size()]),
				valuesToFilterOn.toArray(new Object[valuesToFilterOn.size()]), firstResult, maxResults);
		modelAndView.addObject(IConstants.TOTAL, total);
		modelAndView.addObject(IConstants.ACTIONS, actions);

		modelAndView.setViewName(targetView);
		return modelAndView;
	}

}