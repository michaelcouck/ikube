package ikube.web.service;

import com.google.common.collect.Lists;
import ikube.analytics.IAnalyticsService;
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
import java.util.Collection;
import java.util.List;
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
        consumes = ikube.model.Context.class,
        produces = ikube.model.Context.class)
    public Response create(@Context final HttpServletRequest request) {
        ikube.model.Context context = unmarshall(ikube.model.Context.class, request);
        logger.debug("Create request : " + context.getName());
        analyticsService.create(context);
        return buildJsonResponse(context(context));
    }

    @POST
    @Path(Analyzer.TRAIN)
    @Api(type = "POST",
        uri = "/ikube/service/analyzer/train",
        description = "Trains an analyzer with the data in the request.",
        consumes = ikube.model.Analysis.class,
        produces = ikube.model.Analysis.class)
    @SuppressWarnings({"unchecked"})
    public Response train(@Context final HttpServletRequest request) {
        Analysis<String, String> analysis = unmarshall(Analysis.class, request);
        String data = analysis.getInput();
        String[] inputs = StringUtils.split(data, "\n\r");
        for (final String input : inputs) {
            Analysis<String, String> clone = SerializationUtilities.clone(Analysis.class, analysis);
            clone.setInput(input);
            analyticsService.train(clone);
        }
        return buildJsonResponse(analysis);
    }

    @POST
    @Path(Analyzer.BUILD)
    @Api(type = "POST",
        uri = "/ikube/service/analyzer/build",
        description = "Builds the analyzer generating the model from the data provided, " +
            "returning the context bound to the analyzer.",
        consumes = ikube.model.Analysis.class,
        produces = ikube.model.Context.class)
    @SuppressWarnings("unchecked")
    public Response build(@Context final HttpServletRequest request) {
        Analysis<?, ?> analysis = unmarshall(Analysis.class, request);
        analyticsService.build(analysis);
        return buildJsonResponse(context(analyticsService.getContext(analysis.getContext())));
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
        consumes = ikube.model.Analysis.class,
        produces = ikube.model.Analysis.class)
    @SuppressWarnings("unchecked")
    public Response analyze(@Context final HttpServletRequest request) {
        Analysis<?, ?> analysis = unmarshall(Analysis.class, request);
        analyticsService.analyze(analysis);
        return buildJsonResponse(analysis);
    }

    @POST
    @Path(Analyzer.DESTROY)
    @Api(type = "POST",
        uri = "/ikube/service/analyzer/destroy",
        description = "Destroys an analyzer, and the generated model, freeing resources.",
        consumes = ikube.model.Context.class,
        produces = ikube.model.Context.class)
    @SuppressWarnings("unchecked")
    public Response destroy(@Context final HttpServletRequest request) {
        ikube.model.Context context = unmarshall(ikube.model.Context.class, request);
        analyticsService.destroy(context);
        return buildJsonResponse(context(context));
    }

    @POST
    @Path(Analyzer.CONTEXT)
    @Api(type = "POST",
        uri = "/ikube/service/analyzer/context",
        description = "Returns the context associated with the analyzer specified.",
        consumes = Analysis.class,
        produces = ikube.model.Context.class)
    @SuppressWarnings("unchecked")
    public Response context(@Context final HttpServletRequest request) {
        Analysis<?, ?> analysis = unmarshall(Analysis.class, request);
        return buildJsonResponse(context(analyticsService.getContext(analysis.getContext())));
    }

    @GET
    @Path(Analyzer.CONTEXTS)
    @Api(type = "POST",
        uri = "/ikube/service/analyzer/contexts",
        description = "Returns all the contexts' names defined in the system.",
        consumes = String.class,
        produces = List.class)
    @SuppressWarnings("unchecked")
    public Response contexts() {
        Collection<ikube.model.Context> contexts = Lists.newArrayList();
        Map<String, ikube.model.Context> contextsMap = analyticsService.getContexts();
        for (final Map.Entry<String, ikube.model.Context> contextEntry : contextsMap.entrySet()) {
            contexts.add(context(contextEntry.getValue()));
        }
        return buildJsonResponse(contexts);
    }

    private ikube.model.Context context(final ikube.model.Context context) {
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

        String[] algorithms = new String[context.getAlgorithms().length];
        String[] filters = new String[context.getAlgorithms().length];
        for (int i = 0; i < context.getAlgorithms().length; i++) {
            algorithms[i] = context.getAlgorithms()[i].getClass().getName();
            filters[i] = context.getFilters()[i].getClass().getName();
        }
        cloned.setAlgorithms(algorithms);
        cloned.setFilters(filters);

        return cloned;
    }

}