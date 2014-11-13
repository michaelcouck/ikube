package ikube.web.service;

import ikube.IConstants;
import ikube.analytics.weka.WekaForecastClassifier;
import ikube.model.Analysis;
import ikube.model.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Michael couck
 * @version 01.00
 * @since 12-11-2014
 */
@Component
@Path(GCAnalyzer.GC_ANALYZER)
@Scope(GCAnalyzer.REQUEST)
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
@Api(description = "Bla...")
public class GCAnalyzer extends Resource {

    /**
     * Constants for the paths to the web services.
     */
    public static final String GC_ANALYZER = "/gc-analyzer";
    public static final String REGISTER_COLLECTOR = "/register-collector";
    public static final String UNREGISTER_COLLECTOR = "/unregister-collector";
    public static final String USED_TO_MAX_RATIO_PREDICTION = "/used-to-max-ratio-prediction";

    @Autowired
    private ikube.application.GCAnalyzer gcAnalyzer;

    @POST
    @SuppressWarnings("unchecked")
    @Path(GCAnalyzer.REGISTER_COLLECTOR)
    @Api(description = "Bla...", produces = Boolean.class)
    public Response registerCollector(
            @QueryParam(value = IConstants.ADDRESS) final String address,
            @QueryParam(value = IConstants.PORT) final int port) throws Exception {
        gcAnalyzer.registerCollector(address, port);
        return buildResponse(Boolean.TRUE);
    }

    @POST
    @SuppressWarnings("unchecked")
    @Path(GCAnalyzer.UNREGISTER_COLLECTOR)
    @Api(description = "Bla...", produces = Boolean.class)
    public Response unregisterCollector(
            @QueryParam(value = IConstants.ADDRESS) final String address,
            @QueryParam(value = IConstants.PORT) final int port) throws Exception {
        gcAnalyzer.unregisterCollector(address, port);
        return buildResponse(Boolean.TRUE);
    }

    @GET
    @SuppressWarnings("unchecked")
    @Path(GCAnalyzer.USED_TO_MAX_RATIO_PREDICTION)
    @Api(description = "Bla...", produces = List.class)
    public Response usedToMaxRatioPrediction(
            @QueryParam(value = IConstants.ADDRESS) final String address,
            @QueryParam(value = IConstants.FORECASTS) final int forecasts) {
        List<Analysis> analyses = new ArrayList<>();
        Object[][][] gcData = gcAnalyzer.getGcData(address);
        for (final Object[][] matrix : gcData) {
            // Create the classifier
            Context context = new Context();
            context.setName(address);
            context.setAnalyzer(WekaForecastClassifier.class.getName());
            // Get the vectors of data from the analyzer
            context.setTrainingDatas(matrixToString(matrix));
            analyticsService.create(context);

            // Do the analysis, one by one on the gc data
            Analysis analysis = new Analysis();
            analysis.setContext(context.getName());
            analysis.setInput("-fieldsToForecast,7,-timeStampField,0,-minLag,1,-maxLag,1,-forecasts," + forecasts);
            analyticsService.analyze(analysis);
            analyses.add(analysis);

            analyticsService.destroy(context);
        }

        return buildResponse(analyses);
    }

    String matrixToString(final Object[][] matrix) {
        // Build the data for the context
        StringBuilder stringBuilder = new StringBuilder();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(IConstants.ANALYTICS_DATE_FORMAT);
        for (int i = 0; i < matrix.length; i++) {
            Object[] vector = matrix[i];
            if (i > 0) {
                stringBuilder.append("\n\r");
            }
            for (int j = 0; j < vector.length; j++) {
                Object instance = vector[j];
                if (j > 0) {
                    stringBuilder.append(",");
                }
                if (Date.class.isAssignableFrom(instance.getClass())) {
                    simpleDateFormat.format((Date) instance);
                } else {
                    stringBuilder.append(instance);
                }
            }
        }
        return stringBuilder.toString();
    }

}