package ikube.web.service;

import ikube.analytics.IAnalyticsService;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.SerializationUtilities;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Map;

/**
 * NOTE: The reason that the methods take an {@link javax.servlet.http.HttpServletRequest} and not
 * the Json object directly is because Jersey wants a converter for each type! I know crazzzzzy right,
 * so it is just easier to de-serialize by hand.
 * <p/>
 * This resource(rest api) class exposes the analytics over rest to a client. This class will
 * call the service layer to provide functions like creating the analyzer, training, and building,
 * and of course using the analyzer, i.e. doing anl analysis on a chunk of data.
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
@Api(description = "The analyzer rest resource")
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

    @Autowired
    protected IAnalyticsService analyticsService;

    @POST
    @Path(Analyzer.CREATE)
    @SuppressWarnings("unchecked")
    @Api(type = "POST",
        uri = "/ikube/service/analyzer/create",
        description =
            "Creates an analyzer with the context in the body. Returns the context " +
                "that was posted as a convenience, the analyzer, although " +
                "constructed and referenced in the context, is potentially large, and not " +
                "returned to the user",
        consumes = Context.class,
        produces = Context.class)
    public Response create(final Context context) {
        analyticsService.create(context);
        return buildResponse(context(context));
    }

    @POST
    @Path(Analyzer.TRAIN)
    @Api(type = "POST",
        uri = "/ikube/service/analyzer/train",
        description = "Trains an analyzer with the data in the request.",
        consumes = Analysis.class,
        produces = Analysis.class)
    @SuppressWarnings({"unchecked"})
    public Response train(final Analysis<String, String> analysis) {
        String data = analysis.getInput();
        String[] inputs = StringUtils.split(data, "\n\r");
        for (final String input : inputs) {
            Analysis<String, String> clone = SerializationUtilities.clone(Analysis.class, analysis);
            clone.setInput(input);
            analyticsService.train(clone);
        }
        return buildResponse(analysis);
    }

    @POST
    @Path(Analyzer.BUILD)
    @Api(type = "POST",
        uri = "/ikube/service/analyzer/build",
        description = "Builds the analyzer generating the model from the data provided, " +
            "returning the context bound to the analyzer.",
        consumes = Analysis.class,
        produces = Context.class)
    @SuppressWarnings("unchecked")
    public Response build(final Analysis<?, ?> analysis) {
        analyticsService.build(analysis);
        ikube.model.Context context = analyticsService.getContext(analysis.getContext());
        return buildResponse(context(context));
    }

    /**
     * This method will take an analysis object, classify it using the classifier that is
     * defined in the analysis object and, add the classification results to the analysis object
     * and serialize it for the caller.
     */
    @POST
    @Path(Analyzer.ANALYZE)
    @Api(type = "POST",
        uri = "/ikube/service/analyzer/analyze",
        description = "Analyses the data using the specified analyzer, and returns the analytis " +
            "object, containing among other things the result, and potentially the distribution " +
            "for the instance, and even the distribution for the entire data set.",
        consumes = Analysis.class,
        produces = Analysis.class)
    @SuppressWarnings("unchecked")
    public Response analyze(final Analysis<?, ?> analysis) {
        analyticsService.analyze(analysis);
        return buildResponse(analysis);
    }

    @POST
    @Path(Analyzer.DESTROY)
    @Api(type = "POST",
        uri = "/ikube/service/analyzer/destroy",
        description = "Destroys an analyzer, and the generated model, freeing resources.",
        consumes = Context.class,
        produces = Context.class)
    @SuppressWarnings("unchecked")
    public Response destroy(final Context context) {
        analyticsService.destroy(context);
        return buildResponse(context(context));
    }

    @POST
    @Path(Analyzer.CONTEXT)
    @Api(type = "POST",
        uri = "/ikube/service/analyzer/context",
        description = "Returns the context associated with the analyzer specified.",
        consumes = Analysis.class,
        produces = Context.class)
    @SuppressWarnings("unchecked")
    public Response context(final Analysis<?, ?> analysis) {
        ikube.model.Context context = analyticsService.getContext(analysis.getContext());
        return buildResponse(context(context));
    }

    @GET
    @Path(Analyzer.CONTEXTS)
    @Api(type = "POST",
        uri = "/ikube/service/analyzer/contexts",
        description = "Returns all the contexts' names defined in the system.",
        consumes = String.class,
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

    private Context context(final Context context) {
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
        String[] algorithms = new String[context.getAlgorithms().length];
        String[] filters = new String[context.getAlgorithms().length];
        for (int i = 0; i < context.getAlgorithms().length; i++) {
            algorithms[i] = context.getAlgorithms()[i].getClass().getName();
            if (context.getFilters() != null && context.getFilters().length > i && context.getFilters()[i] != null) {
                filters[i] = context.getFilters()[i].getClass().getName();
            }
        }
        cloned.setAlgorithms(algorithms);
        cloned.setFilters(filters);

        return cloned;
    }

}