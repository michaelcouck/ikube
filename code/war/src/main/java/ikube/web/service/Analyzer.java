package ikube.web.service;

import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.toolkit.SerializationUtilities;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Map;

/**
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
@SuppressWarnings("SpringJavaAutowiringInspection")
public class Analyzer extends Resource {

    public static final String ANALYZER = "/analyzer";
    public static final String CREATE = "/create";
    public static final String TRAIN = "/train";
    public static final String BUILD = "/build";
    public static final String ANALYZE = "/analyze";
    public static final String DESTROY = "/destroy";
    public static final String ANALYZERS = "/analyzers";
    public static final String CONTEXT = "/context";
    public static final String CONTEXTS = "/contexts";

    @Autowired
    protected IAnalyticsService analyticsService;

    @POST
    @Path(Analyzer.CREATE)
    @SuppressWarnings("unchecked")
    public Response create(final ikube.model.Context context /* @Context final HttpServletRequest request */) {
        // ikube.model.Context context = unmarshall(ikube.model.Context.class, request);
        logger.debug("Create request : " + context.getName());
        IAnalyzer analyzer = analyticsService.create(context);
        logger.debug("               : " + analyzer);
        ikube.model.Context response = context(context);
        logger.debug("               : " + response.getName());
        return buildJsonResponse(response);
    }

    @POST
    @Path(Analyzer.TRAIN)
    @SuppressWarnings({"unchecked"})
    public Response train(final Analysis<String, String> analysis /* @Context final HttpServletRequest request */) {
        // Analysis<String, String> analysis = unmarshall(Analysis.class, request);
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
    @SuppressWarnings("unchecked")
    public Response build(final Analysis<?, ?> analysis /* @Context final HttpServletRequest request */) {
        // Analysis<?, ?> analysis = unmarshall(Analysis.class, request);
        analyticsService.build(analysis);
        return buildJsonResponse(context(analysis.getAnalyzer()));
    }

    /**
     * This method will take an analysis object, classify it using the classifier that is
     * defined in the analysis object and, add the classification results to the analysis object
     * and serialize it for the caller.
     */
    @POST
    @Path(Analyzer.ANALYZE)
    @SuppressWarnings("unchecked")
    public Response analyze(final Analysis<?, ?> analysis /* @Context final HttpServletRequest request */) {
        // Analysis<?, ?> analysis = unmarshall(Analysis.class, request);
        analyticsService.analyze(analysis);
        if (analysis.isClassesAndClusters()) {
            analyticsService.classesOrClusters(analysis);
        }
        if (analysis.isSizesForClassesAndClusters()) {
            analyticsService.sizesForClassesOrClusters(analysis);
        }
        return buildJsonResponse(analysis);
    }

    @POST
    @Path(Analyzer.DESTROY)
    @SuppressWarnings("unchecked")
    public Response destroy(final ikube.model.Context context /* @Context final HttpServletRequest request */) {
        // ikube.model.Context context = unmarshall(ikube.model.Context.class, request);
        analyticsService.destroy(context);
        return buildJsonResponse(context);
    }

    @GET
    @Path(Analyzer.ANALYZERS)
    @SuppressWarnings("unchecked")
    public Response analyzers() {
        logger.debug("Analyzers : ");
        Map<String, IAnalyzer> analyzers = analyticsService.getAnalyzers();
        logger.debug("           : " + analyzers);
        String[] names = analyzers.keySet().toArray(new String[analyzers.size()]);
        logger.debug("           : " + Arrays.toString(names));
        return buildJsonResponse(names);
    }

    @POST
    @Path(Analyzer.CONTEXT)
    @SuppressWarnings("unchecked")
    public Response context(final Analysis<?, ?> analysis /* @Context final HttpServletRequest request */) {
        // Analysis<?, ?> analysis = unmarshall(Analysis.class, request);
        return buildJsonResponse(context(analysis.getAnalyzer()));
    }

    @GET
    @Path(Analyzer.CONTEXTS)
    @SuppressWarnings("unchecked")
    public Response contexts() {
        Map<String, Context> contexts = analyticsService.getContexts();
        String[] names = contexts.keySet().toArray(new String[contexts.size()]);
        return buildJsonResponse(names);
    }

    /**
     * This method will just create a new context and null out the analyzer because it is too big to send to the front end.
     *
     * @param analyzerName the name of the analyzer in the system, that is associated with the target context
     * @return the context that was/is used to create the analyzer
     */
    @SuppressWarnings("unchecked")
    private ikube.model.Context context(final String analyzerName) {
        ikube.model.Context contextSystem = analyticsService.getContext(analyzerName);
        return context(contextSystem);
    }

    @SuppressWarnings("unchecked")
    private ikube.model.Context context(final ikube.model.Context contextSystem) {
        ikube.model.Context context = new ikube.model.Context();
        try {
            BeanUtilsBean beanUtilsBean = BeanUtilsBean2.getInstance();
            beanUtilsBean.getConvertUtils().register(new Converter() {
                @Override
                public Object convert(final Class type, final Object value) {
                    if (Timestamp.class.isAssignableFrom(type) && value != null) {
                        return value;
                    }
                    return null;
                }
            }, Timestamp.class);
            beanUtilsBean.copyProperties(context, contextSystem);
            context.setTrainingData(null);
            return context;
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}