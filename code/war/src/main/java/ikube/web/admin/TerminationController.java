package ikube.web.admin;

import ikube.listener.ListenerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This controller will terminate the thread pool that runs the actions, or if the command is 'startup' it will start the thread pool again.
 * 
 * @author Michael Couck
 * @since 10.12.2011
 * @version 01.00
 */
@Controller
public class TerminationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TerminationController.class);

	@Autowired
	private ListenerManager listenerManager;

	public TerminationController() {
		LOGGER.error("TeminationController : ");
	}

	/**
	 * {@inheritDoc}
	 */
	@RequestMapping(value = "/admin/terminate.html", method = RequestMethod.GET)
	public String terminate(@RequestParam(required = true, value = "targetView") String targetView,
			@RequestParam(required = true, value = "command") String command) throws Exception {
		listenerManager.fireEvent(command, System.currentTimeMillis(), null, Boolean.FALSE);
		return "redirect:" + targetView;
	}

}