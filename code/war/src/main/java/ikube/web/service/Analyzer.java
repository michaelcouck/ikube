package ikube.web.service;

import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.toolkit.SerializationUtilities;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

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
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class Analyzer extends Resource {

    public static final String ANALYZER = "/analyzer";
    public static final String CREATE = "/create";
    public static final String TRAIN = "/train";
    public static final String BUILD = "/build";
    public static final String ANALYZE = "/analyze";
    public static final String DESTROY = "/destroy";
    public static final String ANALYZERS = "/analyzers";

    @Autowired
    protected IAnalyticsService analyticsService;

    @POST
    @Path(Analyzer.CREATE)
    @SuppressWarnings("unchecked")
    public Response create(@Context final HttpServletRequest request) {
        ikube.model.Context context = unmarshall(ikube.model.Context.class, request);
        analyticsService.create(context);
        return buildJsonResponse(context.getName());
    }

    @POST
    @Path(Analyzer.TRAIN)
    @SuppressWarnings({"unchecked"})
    public Response train(@Context final HttpServletRequest request) {
        Analysis<String, String> analysis = unmarshall(Analysis.class, request);
        String data = analysis.getInput();
        String[] inputs = StringUtils.split(data, "\n\r\t");
        for (final String input : inputs) {
            Analysis<String, String> clone = SerializationUtilities.clone(Analysis.class, analysis);
            clone.setInput(input);
            analyticsService.train(clone);
        }
        return buildJsonResponse(analysis);
    }

    @POST
    @Path(Analyzer.BUILD)
    @SuppressWarnings("unchecked")
    public Response build(@Context final HttpServletRequest request) {
        ikube.model.Context context = unmarshall(ikube.model.Context.class, request);
        analyticsService.build(context);
        return buildJsonResponse(context.getName());
    }

    /**
     * This method will take an analysis object, classify it using the classifier that is defined in the analysis object and, add the
     * classification results to the analysis object and serialize it for the caller.
     */
    @POST
    @Path(Analyzer.ANALYZE)
    @SuppressWarnings("unchecked")
    public Response analyze(@Context final HttpServletRequest request) {
        Analysis<?, ?> analysis = unmarshall(Analysis.class, request);
        analysis = analyticsService.analyze(analysis);
        String algorithmOutput = analysis.getAlgorithmOutput(); // newLineToLineBreak(analysis.getAlgorithmOutput());
        analysis.setAlgorithmOutput(algorithmOutput);
        return buildJsonResponse(analysis);
    }

    @POST
    @Path(Analyzer.DESTROY)
    @SuppressWarnings("unchecked")
    public Response destroy(@Context final HttpServletRequest request) {
        ikube.model.Context context = unmarshall(ikube.model.Context.class, request);
        analyticsService.destroy(context);
        return buildJsonResponse(context);
    }

    @GET
    @Path(Analyzer.ANALYZERS)
    @SuppressWarnings("unchecked")
    public Response analyzers() {
        Map<String, IAnalyzer> analyzers = analyticsService.getAnalyzers();
        String[] names = analyzers.keySet().toArray(new String[analyzers.size()]);
        return buildJsonResponse(names);
    }

    @SuppressWarnings("UnusedDeclaration")
    private String newLineToLineBreak(final Object object) {
        if (object != null && String.class.isAssignableFrom(object.getClass())) {
            String result = StringUtils.replace(object.toString(), "\n", "<br>");
            return StringUtils.replace(result, "\r", "<br>");
        }
        return null;
    }

}