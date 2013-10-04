package ikube.web.service;

import ikube.IConstants;
import ikube.analytics.IAnalyticsService;
import ikube.model.Analysis;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * TODO Implement this and the architecture of course...
 * 
 * @author Michael couck
 * @since 02.07.13
 * @version 01.00
 */
@Component
@Path(Classifier.CLASSIFIER)
@Scope(Resource.REQUEST)
@Produces(MediaType.APPLICATION_JSON)
public class Classifier extends Resource {

	public static final String CLASSIFY = "/classify";
	public static final String CLASSIFIER = "/classifier/json";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(Classifier.class);

	@Autowired
	protected IAnalyticsService analyticsService;

	/**
	 * This method will take an analysis object, classify it using the classifier that is defined in the analysis object and, add the classification results to
	 * the analysis object and serialize it for the caller.
	 */
	@POST
	@Path(Classifier.CLASSIFY)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response classify(@Context
	final HttpServletRequest request, @Context
	final UriInfo uriInfo) {
		return buildResponse("nothing");
	}

	/**
	 * This method will simply take text input data from the caller, classify it using the classifier that is specified in the parameter list, add the
	 * classification results to the analysis object and serialize it for the caller.
	 */
	@GET
	@Path(Classifier.CLASSIFY)
	@Consumes(MediaType.APPLICATION_XML)
	public Response classify(@QueryParam(value = IConstants.CLASSIFIER)
	final String analyzer, @QueryParam(value = IConstants.CONTENT)
	final String content) throws IOException {
		Analysis<String, String> analysis = new Analysis<String, String>();
		analysis.setInput(content);
		analysis.setAnalyzer(analyzer);
		analyticsService.analyze(analysis);
		return buildResponse(analysis);
	}

}