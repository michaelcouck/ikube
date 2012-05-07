package ikube.web.admin;

import ikube.IConstants;
import ikube.database.IDataBase;
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
	 * TODO Comment this method.
	 */
	@RequestMapping(value = "/admin/database/sorted.html", method = RequestMethod.GET)
	public ModelAndView selectWithSort(@RequestParam(required = true, value = "targetView") String targetView,
			@RequestParam(required = true, value = "classType") String classType,
			@RequestParam(required = true, value = "sortFields") List<String> sortFields,
			@RequestParam(required = true, value = "directionOfSort") List<Boolean> directionOfSort,
			@RequestParam(required = true, value = "firstResult") int firstResult,
			@RequestParam(required = true, value = "maxResults") int maxResults, ModelAndView modelAndView) throws Exception {
		String[] sortFieldsArray = sortFields.toArray(new String[sortFields.size()]);
		Boolean[] directionOfSortArray = directionOfSort.toArray(new Boolean[directionOfSort.size()]);
		List<?> list = dataBase.find(Class.forName(classType), sortFieldsArray, directionOfSortArray, firstResult, maxResults);
		setProperties(classType, list, new String[0], new Object[0], targetView, modelAndView);
		return modelAndView;
	}

	/**
	 * TODO Comment this method.
	 */
	@RequestMapping(value = "/admin/database/filtered.html", method = RequestMethod.GET)
	public ModelAndView selectWithFiltering(@RequestParam(required = true, value = "targetView") String targetView,
			@RequestParam(required = true, value = "classType") String classType,
			@RequestParam(required = true, value = "fieldsToFilterOn") List<String> fieldsToFilterOn,
			@RequestParam(required = true, value = "valuesToFilterOn") List<Object> valuesToFilterOn,
			@RequestParam(required = true, value = "firstResult") long firstResult,
			@RequestParam(required = true, value = "maxResults") long maxResults, ModelAndView modelAndView) throws Exception {
		String[] fieldsToFilterOnArray = fieldsToFilterOn.toArray(new String[fieldsToFilterOn.size()]);
		Object[] valuesToFilterOnArray = valuesToFilterOn.toArray(new Object[valuesToFilterOn.size()]);
		List<?> list = dataBase.findCriteria(Class.forName(classType), fieldsToFilterOnArray, valuesToFilterOnArray, (int) firstResult,
				(int) maxResults);
		setProperties(classType, list, fieldsToFilterOnArray, valuesToFilterOnArray, targetView, modelAndView);
		return modelAndView;
	}

	private void setProperties(final String classType, final List<?> list, final String[] fieldsToFilterOnArray,
			final Object[] valuesToFilterOnArray, final String targetView, final ModelAndView modelAndView) throws Exception {
		Class<?> klass = Class.forName(classType);
		Map<String, Object> parameters = new HashMap<String, Object>();
		for (int i = 0; i < fieldsToFilterOnArray.length; i++) {
			parameters.put(fieldsToFilterOnArray[i], valuesToFilterOnArray[i]);
		}
		Long total = dataBase.count(klass, parameters);
		List<String> fieldNames = DatabaseUtilities.getFieldNames(klass, new ArrayList<String>());
		Server server = clusterManager.getServer();

		modelAndView.addObject(IConstants.TOTAL, total);
		modelAndView.addObject(IConstants.ENTITIES, list);
		modelAndView.addObject(IConstants.FIELD_NAMES, fieldNames);
		modelAndView.addObject(IConstants.SERVER, server);
		modelAndView.setViewName(targetView);
	}

}