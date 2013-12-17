package ikube.web.service;

import ikube.analytics.IAnalyticsService;
import ikube.model.Search;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.spring.Autowire;

/**
 * Strangely enough, if this class name is changed to Twitter Spring and Jersey do not inject the services. Hmmm... what's in a name?
 * 
 * @author Michael couck
 * @since 17.12.13
 * @version 01.00
 */
@Provider
@Autowire
@Component
@Path(Anal.TWITTER)
@Scope(Resource.REQUEST)
@Produces(MediaType.APPLICATION_JSON)
public class Anal extends Resource {

	public static final String TWITTER = "/twitter";
	public static final String ANALYZE = "/analyze";

	@Autowired
	protected IAnalyticsService analyticsService;

	@POST
	@Path(Anal.ANALYZE)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response twitter(@Context final HttpServletRequest request, @Context final UriInfo uriInfo) {
		Search search = unmarshall(Search.class, request);
		Object results = searcherService.search(search);
		return buildJsonResponse(results);
	}

}