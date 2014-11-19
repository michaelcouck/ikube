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
 * Please read the description in the annotation.
 *
 * @author Michael couck
 * @version 01.00
 * @since 12-11-2014
 */
@Component
@Path(GCAnalyzer.GC_ANALYZER)
@Scope(GCAnalyzer.REQUEST)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(description = "This service is the rest front end for accessing the garbage collection data. A " +
        "{@link ikube.application.GCCollector} can be added to a particular address(ip), which " +
        "will then collect garbage collection data. This data can then be used to predict the " +
        "memory and garbage collection telemetry for a period of time. For example if a collector " +
        "is registered for a machine that typically runs out of memory, then after a certain time, " +
        "the prediction for the used memory to max ratio will decrease, indicating that the machine " +
        "will run out of memory, i.e. when the ratio is close to zero.")
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

    /**
     * Please read the description in the annotation.
     */
    @POST
    @SuppressWarnings("unchecked")
    @Path(GCAnalyzer.REGISTER_COLLECTOR)
    @Api(description = "This method attached a collector to a particular JVM, via JMX. " +
            "@param address the address of the target JVM. " +
            "Note that the JVM must be started with the following parameters:" +
            "-Djava.rmi.server.hostname=localhost " +
            "-Dcom.sun.management.jmxremote " +
            "-Dcom.sun.management.jmxremote.port=8500 " +
            "-Dcom.sun.management.jmxremote.rmi.port=8500 " +
            "-Dcom.sun.management.jmxremote.local.only=false " +
            "-Dcom.sun.management.jmxremote.authenticate=false " +
            "-Dcom.sun.management.jmxremote.ssl=false " +
            "The port defined muse be the same as the one used to connect in the parameter list. " +
            "@param port the port to connect to the remote JVM " +
            "@return whether the connect operation was successful for the remove JMX machine",
            produces = Boolean.class)
    public Response registerCollector(
            @QueryParam(value = IConstants.ADDRESS) final String address,
            @QueryParam(value = IConstants.PORT) final int port) throws Exception {
        gcAnalyzer.registerCollector(address, port);
        return buildResponse(Boolean.TRUE);
    }

    /**
     * Please read the description in the annotation.
     */
    @POST
    @SuppressWarnings("unchecked")
    @Path(GCAnalyzer.UNREGISTER_COLLECTOR)
    @Api(description = "This method removes a collector from a particular JVM, destroying the connection and other resources. " +
            "@param the address for the remove machine. " +
            "@param port the port of the remote machine to remove the collector from",
            produces = Boolean.class)
    public Response unregisterCollector(
            @QueryParam(value = IConstants.ADDRESS) final String address,
            @QueryParam(value = IConstants.PORT) final int port) throws Exception {
        gcAnalyzer.unregisterCollector(address, port);
        return buildResponse(Boolean.TRUE);
    }

    /**
     * Please read the description in the annotation.
     */
    @GET
    @SuppressWarnings("unchecked")
    @Path(GCAnalyzer.USED_TO_MAX_RATIO_PREDICTION)
    @Api(description = "This method will get all the collected data from the specified collector(s) for the " +
            "address specified. The collected data will be converted into a usable form for Weka time series classifier. " +
            "A prediction will be made to calculate the used memory to max memory ratio for the JVM, for separate memory blocks," +
            "like eden, perm and old(tenured). This prediction will then be returned as one analysis for each of the memory " +
            "blocks." +
            "@param the address of the target JVM to get teh predictions for" +
            "@param the port of the target JVM to get the predictions for" +
            "@param the number of predictions to generate, in minutes, i.e. 60 minutes for example is ample",
            produces = List.class)
    public Response usedToMaxRatioPrediction(
            @QueryParam(value = IConstants.ADDRESS) final String address,
            @QueryParam(value = IConstants.PORT) final int port,
            @QueryParam(value = IConstants.FORECASTS) final int forecasts) {
        List<Analysis> analyses = new ArrayList<>();
        Object[][][] gcData = gcAnalyzer.getGcData(address, port);
        for (final Object[][] matrix : gcData) {
            if (matrix == null || matrix.length < 3) {
                // We must have some data to make a prediction, less than 3
                // vectors is probably not enough to make a reasonable prediction
                // and means that the Jvm immediately runs out of memory on startup,
                // we can't do anything about that
                continue;
            }
            // Create the classifier
            Context context = new Context();
            context.setName(address);
            context.setAnalyzer(WekaForecastClassifier.class.getName());
            // Get the vectors of data from the analyzer
            String stringMatrix = matrixToString(matrix);
            if (logger.isDebugEnabled()) {
                logger.error("Matrix to time series analyze : " + stringMatrix);
            }
            context.setTrainingDatas(stringMatrix);
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
                    stringBuilder.append(simpleDateFormat.format((Date) instance));
                } else {
                    stringBuilder.append(instance);
                }
            }
        }
        return stringBuilder.toString();
    }

}