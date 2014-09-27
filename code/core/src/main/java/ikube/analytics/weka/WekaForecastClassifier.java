package ikube.analytics.weka;

import ikube.model.Analysis;
import ikube.model.Context;
import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.timeseries.WekaForecaster;
import weka.core.Instances;

import java.util.List;

import static ikube.Constants.DELIMITER_CHARACTERS;
import static org.apache.commons.lang.StringUtils.split;

/**
 * This class will predict a value or values based on previous values in time. The trend for the
 * series is layed over the initial input data(typically a matrix), and the previous values for the time
 * series converted to fields in the vectors.
 *
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
    public synchronized Analysis<Object, Object> analyze(final Context context, final Analysis analysis) throws Exception {
        // For the forecaster we have already built the models, we just need
        // to specify the options for the forecast, like the number is days into the future etc.
        WekaForecasterClassifierOption option = new WekaForecasterClassifierOption((Object[]) analysis.getInput());

        Object[] models = context.getModels();
        String[] fieldsToForecast = split(option.getFieldsToForecast(), DELIMITER_CHARACTERS);
        // This structure is model => how many days of forecasts => fields to forecast
        double[][][] predictions = new double[models.length][option.getForecasts()][fieldsToForecast.length];

        for (int i = 0; i < models.length; i++) {
            Instances instances = (Instances) models[i];

            WekaForecaster forecaster = new WekaForecaster();
            forecaster.setFieldsToForecast(option.getFieldsToForecast()); // ***** The fields to forecast, 6,3 for example

            forecaster.getTSLagMaker().setTimeStampField(option.getTimeStampField()); // ***** The time field, i.e. the date field
            forecaster.getTSLagMaker().setMinLag(option.getMinLag());
            forecaster.getTSLagMaker().setMaxLag(option.getMaxLag()); // ***** The units to use, 12=months, 1=days

            // add a month of the year indicator field
            forecaster.getTSLagMaker().setAddMonthOfYear(true);
            // add a quarter of the year indicator field
            forecaster.getTSLagMaker().setAddQuarterOfYear(true);
            // build the model
            forecaster.buildForecaster(instances, System.out);
            // prime the forecaster with enough recent historical data
            // to cover up to the maximum lag. In our case, we could just supply
            // the 12 most recent historical instances, as this covers our maximum
            // lag period
            forecaster.primeForecaster(instances);
            // forecast for 12 units (months) beyond the end of the training data
            List<List<NumericPrediction>> forecast = forecaster.forecast(option.getForecasts(), System.out); // ***** The number of predictions into the future
            // output the predictions. Outer list is over the steps; inner list is over the targets
            logger.warn("Confidence : " + forecaster.getConfidenceLevel());

            for (int j = 0; j < forecast.size(); j++) {
                List<NumericPrediction> predictionsAtStep = forecast.get(j);
                for (int k = 0; k < predictionsAtStep.size(); k++) {
                    NumericPrediction predictionAtStep = predictionsAtStep.get(k);
                    logger.warn("Prediction : " + predictionAtStep.predicted());
                    predictions[i][j][k] += predictionAtStep.predicted();
                }
            }
        }

        // Aggregate the predictions
        for (int i = 0; i < predictions.length; i++) {
            for (int j = 0; j < predictions[i].length; j++) {
                for (int k = 0; k < predictions[i][j].length; k++) {
                    predictions[i][j][k] = predictions[i][j][k] / models.length;
                }
            }
        }
        //noinspection unchecked
        analysis.setOutput(predictions);

        //noinspection unchecked
        return analysis;
    }

}