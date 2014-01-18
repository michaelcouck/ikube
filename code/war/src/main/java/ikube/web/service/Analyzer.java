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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * TODO Implement this and the architecture of course...
 *
 * @author Michael couck
 * @version 01.00
 * @since 02.07.13
 */
@Component
@Path(Analyzer.ANALYZER)
@Scope(Resource.REQUEST)
@Produces(MediaType.APPLICATION_JSON)
public class Analyzer extends Resource {

    public static final String ANALYZER = "/analyzer";
    public static final String ANALYZE = "/analyze";

    @Autowired
    protected IAnalyticsService analyticsService;

    /**
     * This method will take an analysis object, classify it using the classifier that is defined in the analysis object and, add the classification results to
     * the analysis object and serialize it for the caller.
     */
    @POST
    @Path(Analyzer.ANALYZE)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response analyze(@Context final HttpServletRequest request, @Context final UriInfo uriInfo) {
        Analysis<?, ?> analysis = unmarshall(Analysis.class, request);
        analyticsService.analyze(analysis);
        analysis.setAlgorithmOutput(newLineToLineBreak(analysis.getAlgorithmOutput()).toString());
        return buildJsonResponse(analysis);
    }

    /**
     * This method will simply take text input data from the caller, classify it using the classifier that is specified in the parameter list, add the
     * classification results to the analysis object and serialize it for the caller.
     */
    @GET
    @Path(Analyzer.ANALYZE)
    @Consumes(MediaType.APPLICATION_XML)
    public Response analyze(@QueryParam(value = IConstants.ANALYZER) final String analyzer, @QueryParam(value = IConstants.CONTENT) final String content)
        throws IOException {
        Analysis<String, String> analysis = new Analysis<>();
        analysis.setInput(content);
        analysis.setAnalyzer(analyzer);
        analyticsService.analyze(analysis);
        analysis.setAlgorithmOutput(newLineToLineBreak(analysis.getAlgorithmOutput()).toString());
        return buildJsonResponse(analysis);
    }

    private Object newLineToLineBreak(final Object object) {
        if (object != null && String.class.isAssignableFrom(object.getClass())) {
            Object result = StringUtils.replace(object.toString(), "\n", "<br>");
            result = StringUtils.replace(result.toString(), "\r", "<br>");
            return result;
        }
        return object;
    }

}