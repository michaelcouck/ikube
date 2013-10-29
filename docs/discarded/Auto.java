package ikube.web.service;

import ikube.IConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	@GET
	@Path(Auto.COMPLETE)
	@Consumes(MediaType.APPLICATION_XML)
	public String autocomplete(@QueryParam(value = IConstants.TERM) final String term) {
		String[] suggestions = null;
		if (StringUtils.isEmpty(term)) {
			suggestions = new String[0];
		} else {
			suggestions = suggestions(term);
			for (int i = 0; i < suggestions.length; i++) {
				suggestions[i] = StringUtils.remove(StringUtils.remove(suggestions[i], "["), "]");
			}
		}
		String result = gson.toJson(suggestions);
		LOGGER.info("Term : " + term + ", " + result);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	String[] suggestions(final String searchString) {
		boolean first = Boolean.TRUE;
		StringTokenizer stringTokenizer = new StringTokenizer(searchString, " ,;|.");
		StringBuilder stringBuilder = new StringBuilder();
		while (stringTokenizer.hasMoreTokens()) {
			String token = stringTokenizer.nextToken();
			if (!first) {
				stringBuilder.append(" AND ");
			}
			stringBuilder.append(token);
			first = Boolean.FALSE;
		}
		ArrayList<HashMap<String, String>> results = searcherService.search(IConstants.AUTOCOMPLETE, new String[] { stringBuilder.toString() },
				new String[] { IConstants.CONTENT }, Boolean.TRUE, 0, 100);
		if (results.size() > 0) {
			results.remove(results.size() - 1);
		}
		Set<String> suggestions = new TreeSet<String>();
		for (final HashMap<String, String> result : results) {
			suggestions.add(result.get(IConstants.CONTENT));
			if (suggestions.size() >= 10) {
				break;
			}
		}
		return suggestions.toArray(new String[suggestions.size()]);
	}

}