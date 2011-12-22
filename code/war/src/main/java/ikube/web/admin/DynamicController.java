package ikube.web.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.Action;
import ikube.model.Index;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Michael Couck
 * @since 22.12.2011
 * @version 01.00
 */
@Controller
public class DynamicController {

	private static final String ACTIONS_VIEW = "/admin/actions";

	@Autowired
	private IDataBase dataBase;

	/**
	 * {@inheritDoc}
	 */
	@RequestMapping(value = ACTIONS_VIEW + ".html", method = RequestMethod.GET)
	public String actions(@RequestParam(required = false, value = "targetView") String targetView,
			@RequestParam(required = true, value = "start") int start, @RequestParam(required = true, value = "end") int end, Model model,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.ACTION_NAME, Index.class.getSimpleName());
		Long total = dataBase.execute(Long.class, Action.SELECT_FROM_ACTIONS_BY_NAME_COUNT, parameters);
		List<Action> actions = dataBase.find(Action.class, Action.SELECT_FROM_ACTIONS_BY_NAME_DESC, parameters,  start, end);
		model.addAttribute(IConstants.TOTAL, total);
		model.addAttribute(IConstants.ACTIONS, actions);
		if (targetView != null) {
			return "redirect:" + targetView;
		}
		return ACTIONS_VIEW;
	}

}