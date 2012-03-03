package ikube.web.admin;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.Search;
import ikube.model.Server;
import ikube.toolkit.DatabaseUtilities;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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

	/** The default forward view for the action. */
	private static final String DATABASE_VIEW = "/admin/database";

	/** Database access for fetching the entities that are stored in ikube. */
	@Autowired
	private IDataBase dataBase;

	/**
	 * {@inheritDoc}
	 */
	@RequestMapping(value = DATABASE_VIEW + ".html", method = RequestMethod.GET)
	public String entities(@RequestParam(required = false, value = "targetView") String targetView,
			@RequestParam(required = true, value = "classType") String classType,
			@RequestParam(required = true, value = "start") int start, @RequestParam(required = true, value = "end") int end, Model model)
			throws Exception {
		Class<?> klass = Class.forName(classType);
		Long total = dataBase.count(Search.class);
		List<?> list = dataBase.find(klass, start, end);
		model.addAttribute(IConstants.TOTAL, total);
		model.addAttribute(IConstants.ENTITIES, list);
		List<String> fieldNames = DatabaseUtilities.getFieldNames(klass, new ArrayList<String>());
		model.addAttribute(IConstants.FIELD_NAMES, fieldNames);

		Server server = clusterManager.getServer();
		model.addAttribute(IConstants.SERVER, server);

		if (targetView != null) {
			return "redirect:" + targetView;
		}
		return DATABASE_VIEW;
	}

}