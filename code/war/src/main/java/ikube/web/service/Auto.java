package ikube.web.service;

import ikube.model.Search;
import ikube.toolkit.SerializationUtilities;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Auto extends Resource {

	static final Logger LOGGER = LoggerFactory.getLogger(Auto.class);

	/** Constants for the paths to the web services. */
	public static final String AUTO = "/auto";
	public static final String SUGGEST = "/suggest";

	@POST
	public Response auto(//
			@Context final HttpServletRequest request, //
			@Context final UriInfo uriInfo) {
		Search search = unmarshall(Search.class, request);
		List<String> searchStrings = search.getSearchStrings();
		for (final String searchString : searchStrings) {
			String[] words = StringUtils.split(searchString, ' ');
			Search cloneSearch = (Search) SerializationUtilities.clone(search);
			for (final String word : words) {
				cloneSearch.setSearchStrings(Arrays.asList(word));
			}
		}
		return buildJsonResponse(search);
	}

	@POST
	@Path(Auto.SUGGEST)
	public Response suggestions(//
			@Context final HttpServletRequest request, //
			@Context final UriInfo uriInfo) {
		Search search = unmarshall(Search.class, request);
		Object results = searcherService.search(search);
		return buildJsonResponse(results);
	}

}