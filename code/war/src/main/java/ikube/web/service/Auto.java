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

import com.google.gson.Gson;

/**
 * @author Michael couck
 * @since 01.03.12
 * @version 01.00
 */
@Component
@Path(Auto.AUTO)
@Scope(Auto.REQUEST)
@Produces(MediaType.TEXT_PLAIN)
public class Auto {

	private static final Logger LOGGER = LoggerFactory.getLogger(Auto.class);

	/** Constants for the paths to the web services. */
	public static final String AUTO = "/auto";
	public static final String COMPLETE = "/complete";
	public static final String REQUEST = "request";

	private Gson gson;

	@Autowired
	private IAutoCompleteService autoCompleteService;

	public Auto() {
		gson = new Gson();
	}

	@GET
	@Path(Auto.COMPLETE)
	@Consumes(MediaType.APPLICATION_XML)
	public String autocomplete(@QueryParam(value = IConstants.TERM) final String term) {
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