package ikube.web.service;

import ikube.IConstants;
import ikube.service.IAutoCompleteService;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Michael couck
 * @since 01.03.12
 * @version 01.00
 */
@Component
@Path(Auto.AUTO)
@Scope(Auto.REQUEST)
@Produces(MediaType.TEXT_PLAIN)
public class Auto extends Resource {

	private static final Logger LOGGER = LoggerFactory.getLogger(Auto.class);

	/** Constants for the paths to the web services. */
	public static final String AUTO = "/auto";
	public static final String COMPLETE = "/complete";
	public static final String REQUEST = "request";

	@Autowired
	private IAutoCompleteService autoCompleteService;

	@GET
	@Path(Auto.COMPLETE)
	@Consumes(MediaType.APPLICATION_XML)
	public String autocomplete(@QueryParam(value = IConstants.TERM) final String term) {
		if (StringUtils.isEmpty(term)) {
			return gson.toJson(new String[0]);
		}
		String[] suggestions = autoCompleteService.suggestions(term);
		for (int i = 0; i < suggestions.length; i++) {
			suggestions[i] = StringUtils.remove(suggestions[i], "[");
			suggestions[i] = StringUtils.remove(suggestions[i], "]");
		}
		String result = gson.toJson(suggestions);
		LOGGER.info("Term : " + term + ", " + result);
		return result;
	}

}