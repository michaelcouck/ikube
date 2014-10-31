package ikube.analytics.weka;

import ikube.IConstants;
import ikube.model.Analysis;
import ikube.model.Context;
import org.apache.commons.lang.StringUtils;
import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.timeseries.WekaForecaster;
import weka.core.Instances;

import java.util.List;

import static ikube.Constants.DELIMITER_CHARACTERS;
import static org.apache.commons.lang.StringUtils.split;

/**
 * This class will predict a value or values based on previous values in time. The trend for the
 * series is used as an overlay for the initial input data(typically a matrix), and the previous values for the time
 * series converted to fields in the vectors.
 * <p/>
 * Multiple fields in the vectors can be predicted in parallel, as well as multiple events into the future.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 23-09-2014
 */
public class WekaForecastClassifier extends WekaClassifier {

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public synchronized Analysis<Object, Object> analyze(final Context context, final Analysis analysis) throws Exception {
        // For the forecaster we have already built the models, we just need
        // to specify the options for the forecast, like the number is days into the future etc.
        Object input = analysis.getInput();
        if (String.class.isAssignableFrom(input.getClass())) {
            input = StringUtils.split(input.toString(), IConstants.DELIMITER_CHARACTERS);
        } else if (List.class.isAssignableFrom(input.getClass())) {
            input = ((List) input).toArray();
        } else if (!Object[].class.isAssignableFrom(input.getClass())) {
            throw new RuntimeException("Input for time series classifier must be of object array : " + input);
        }
        WekaForecasterClassifierOption option = new WekaForecasterClassifierOption((Object[]) input);

        Object[] models = context.getModels();
        String[] fieldsToForecast = split(option.getFieldsToForecast(), DELIMITER_CHARACTERS);
        // This structure is model => how many days of forecasts => fields to forecast
        double[][][] predictions = new double[models.length][option.getForecasts()][fieldsToForecast.length];

        for (int i = 0; i < models.length; i++) {
            Instances instances = (Instances) models[i];
            // Always sort the instances in ascending order
            instances.sort(instances.attribute(option.getTimeStampField()));

            WekaForecaster forecaster = new WekaForecaster();
            forecaster.setFieldsToForecast(option.getFieldsToForecast()); // ***** The fields to forecast, 6,3 for example

            forecaster.getTSLagMaker().setTimeStampField(option.getTimeStampField()); // ***** The time field, i.e. the date field
            forecaster.getTSLagMaker().setMinLag(option.getMinLag());
            forecaster.getTSLagMaker().setMaxLag(option.getMaxLag()); // ***** The units to use, 12=months, 1=days

            // Add a month of the year indicator field
            forecaster.getTSLagMaker().setAddMonthOfYear(true);
            // Add a quarter of the year indicator field
            forecaster.getTSLagMaker().setAddQuarterOfYear(true);
            // Build the model
            forecaster.buildForecaster(instances, System.out);
            // Prime the forecaster with enough recent historical data to cover up to the maximum lag. In
            // our case, we could just supply the 12 most recent historical instances, as this covers our maximum
            // lag period
            forecaster.primeForecaster(instances);
            // Forecast for 12 units (months) beyond the end of the training data
            List<List<NumericPrediction>> forecast = forecaster.forecast(option.getForecasts(), System.out); // ***** The number of predictions into the future
            // Output the predictions. Outer list is over the steps; inner list is over the targets
            logger.warn("Confidence : " + forecaster.getConfidenceLevel());

            for (int j = 0; j < forecast.size(); j++) {
                List<NumericPrediction> predictionsAtStep = forecast.get(j);
                for (int k = 0; k < predictionsAtStep.size(); k++) {
                    NumericPrediction predictionAtStep = predictionsAtStep.get(k);
                    logger.warn("Prediction : " + predictionAtStep.predicted());
                    predictions[i][j][k] += predictionAtStep.predicted();
                }
            }

            analysis.setAlgorithmOutput(forecaster.getBaseForecaster().toString());
        }

        // Aggregate the predictions
        for (int i = 0; i < predictions.length; i++) {
            for (int j = 0; j < predictions[i].length; j++) {
                for (int k = 0; k < predictions[i][j].length; k++) {
                    predictions[i][j][k] = predictions[i][j][k] / models.length;
                }
            }
        }
        analysis.setOutput(predictions);

        return analysis;
    }

}