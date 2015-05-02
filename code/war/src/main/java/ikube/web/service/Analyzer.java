package ikube.web.service;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import ikube.analytics.IAnalyticsService;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.SERIALIZATION;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import static ikube.toolkit.MATRIX.invertMatrix;

/**
 * This resource(rest api) class exposes the analytics over rest to a client. This class will
 * call the service layer to provide functions like creating the analyzer, training, and building,
 * and of course using the analyzer, i.e. doing an analysis on a chunk of data.
 *
 * @author Michael couck
 * @version 01.00
 * @since 02-07-2013
 */
@Component
@Path(Analyzer.ANALYZER)
@Scope(Resource.REQUEST)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(description = "This resource is the api for the analytics functionality. Methods that are exposed in this " +
        "web service are creating and analyzer, training, building and using, etc. Please refer to the individual methods " +
        "for more information on how to use the api.")
@SuppressWarnings("SpringJavaAutowiringInspection")
public class Analyzer extends Resource {

    public static final String ANALYZER = "/analyzer";
    public static final String CREATE = "/create";
    public static final String TRAIN = "/train";
    public static final String BUILD = "/build";
    public static final String ANALYZE = "/analyze";
    public static final String DESTROY = "/destroy";
    public static final String CONTEXT = "/context";
    public static final String CONTEXTS = "/contexts";
    public static final String UPLOAD = "/upload";
    public static final String DATA = "/data";
    public static final String CREATE_BUILD_ANALYZE_DESTROY = "/create-build-analyze-destroy";

    @Autowired
    protected IAnalyticsService analyticsService;

    @POST
    @Path(Analyzer.CREATE)
    @SuppressWarnings("unchecked")
    @Api(description =
            "Creates an analyzer with the context in the body. Returns the context " +
                    "that was posted as a convenience, the analyzer, although " +
                    "constructed and referenced in the context, is potentially large, and not " +
                    "returned to the user",
            produces = Context.class)
    public Response create(final Context context) {
        analyticsService.create(context);
        return buildResponse(context(context));
    }

    @POST
    @Path(Analyzer.UPLOAD)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Api(description = "This will upload a file to be used in the creation of an analyzer.",
            produces = String.class)
    public Response upload(@FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
        return buildResponse(analyticsService.upload(fileDetail.getFileName(), inputStream));
    }

    @POST
    @Path(Analyzer.DATA)
    @Api(description = "This method will just get some data that is attached to the analyzer.",
            produces = Analysis.class)
    @SuppressWarnings({"unchecked"})
    public Response data(final Context context) {
        Object[][][] matrices = analyticsService.data(context, 9);
        Object[][][] invertedMatrices = new Object[matrices.length][][];
        for (int i = 0; i < matrices.length; i++) {
            invertedMatrices[i] = invertMatrix(matrices[i]);
        }
        return buildResponse(invertedMatrices);
    }

    @POST
    @Path(Analyzer.TRAIN)
    @Api(description = "Trains an analyzer with the data in the request. This method adds the " +
            "data in the analysis object to the input data for the analyzer, could be a classifier or " +
            "a clusterer.",
            produces = Analysis.class)
    @SuppressWarnings({"unchecked"})
    public Response train(final Analysis<String, String> analysis) {
        String data = analysis.getInput();
        String[] inputs = StringUtils.split(data, "\n\r");
        for (final String input : inputs) {
            Analysis<String, String> clone = SERIALIZATION.clone(Analysis.class, analysis);
            clone.setInput(input);
            analyticsService.train(clone);
        }
        return buildResponse(analysis);
    }

    @POST
    @Path(Analyzer.BUILD)
    @Api(description = "Builds the analyzer generating the model from the data provided, " +
            "returning the context bound to the analyzer. In the case of classifiers for example, the data " +
            "is probably converted into vectors for a support vector machine, as an example.",
            produces = Context.class)
    @SuppressWarnings("unchecked")
    public Response build(final Analysis analysis) {
        analyticsService.build(analysis);
        ikube.model.Context context = analyticsService.getContext(analysis.getContext());
        return buildResponse(context(context));
    }

    @POST
    @Path(Analyzer.ANALYZE)
    @Api(description = "Analyses the data using the specified analyzer, and returns the analysis " +
            "object, containing among other things the result, and potentially the distribution " +
            "for the instance, and even the distribution for the entire data set.",
            produces = Analysis.class)
    @SuppressWarnings("unchecked")
    public Response analyze(final Analysis analysis) {
        analyticsService.analyze(analysis);
        return buildResponse(analysis);
    }

    @POST
    @Path(Analyzer.CREATE_BUILD_ANALYZE_DESTROY)
    @Api(description = "Creates an analyzer, builds it(mandatory), uses it for the analysis" +
            "and finally destroys it releasing resources, returning the analysis object with the " +
            "analysis result.",
            produces = Context.class)
    @SuppressWarnings("unchecked")
    public Response createBuildAnalyzeDestroy(final Context context, final String input) {
        Analysis<String, String> analysis = new Analysis<>();
        analysis.setContext(context.getName());
        analysis.setInput(input);
        try {
            create(context);
            build(analysis);
            return analyze(analysis);
        } finally {
            destroy(context);
        }
    }

    @POST
    @Path(Analyzer.DESTROY)
    @Api(description = "Destroys an analyzer, and the generated model, freeing resources.",
            produces = Context.class)
    @SuppressWarnings("unchecked")
    public Response destroy(final Context context) {
        analyticsService.destroy(context);
        return buildResponse(context(context));
    }

    @POST
    @Path(Analyzer.CONTEXT)
    @Api(description = "Returns the context associated with the analyzer specified.",
            produces = Context.class)
    @SuppressWarnings("unchecked")
    public Response context(final Analysis analysis) {
        ikube.model.Context context = analyticsService.getContext(analysis.getContext());
        return buildResponse(context(context));
    }

    @GET
    @Path(Analyzer.CONTEXTS)
    @Api(description = "Returns all the contexts' names defined in the system.",
            produces = ArrayList.class)
    @SuppressWarnings("unchecked")
    public Response contexts() {
        Map<String, ikube.model.Context> contextsMap = analyticsService.getContexts();
        ArrayList contexts = new ArrayList();
        for (final Map.Entry<String, ikube.model.Context> contextEntry : contextsMap.entrySet()) {
            contexts.add(contextEntry.getKey());
        }
        return buildResponse(contexts.toArray());
    }

    Context context(final Context context) {
        if (context == null) {
            return null;
        }
        ikube.model.Context cloned = new ikube.model.Context();
        cloned.setBuilt(context.isBuilt());
        cloned.setEvaluations(context.getEvaluations());
        cloned.setFileNames(context.getFileNames());

        cloned.setMaxTrainings(context.getMaxTrainings());
        cloned.setName(context.getName());
        cloned.setOptions(context.getOptions());

        if (String.class.isAssignableFrom(context.getAnalyzer().getClass())) {
            cloned.setAnalyzer(context.getAnalyzer());
        } else {
            cloned.setAnalyzer(context.getAnalyzer().getClass().getName());
        }
        if (context.getAlgorithms() != null) {
            String[] algorithms = new String[context.getAlgorithms().length];
            for (int i = 0; i < context.getAlgorithms().length; i++) {
                algorithms[i] = context.getAlgorithms()[i].getClass().getName();
            }
            cloned.setAlgorithms(algorithms);
        }
        if (context.getFilters() != null) {
            String[] filters = new String[context.getFilters().length];
            for (int i = 0; i < context.getFilters().length; i++) {
                if (context.getFilters().length > i && context.getFilters()[i] != null) {
                    filters[i] = context.getFilters()[i].getClass().getName();
                }
            }
            cloned.setFilters(filters);
        }
        return cloned;
    }

}