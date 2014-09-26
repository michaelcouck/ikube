package ikube.analytics.weka;

import ikube.model.Analysis;
import ikube.model.Context;
import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.timeseries.WekaForecaster;
import weka.core.Instances;

import java.util.List;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-09-2014
 */
public class WekaForecastClassifier extends WekaClassifier {

    /**
     * {@inheritDoc}
     */
    public synchronized Analysis<Object, Object> analyze(final Context context, final Analysis analysis) throws Exception {
        Object[] models = context.getModels();

        for (final Object model : models) {
            Instances instances = (Instances) model;

            WekaForecaster forecaster = new WekaForecaster();
            forecaster.setFieldsToForecast("6"); // ***** The fields to forecast

            forecaster.getTSLagMaker().setTimeStampField("0"); // ***** The time field
            forecaster.getTSLagMaker().setMinLag(1);
            forecaster.getTSLagMaker().setMaxLag(1); // ***** The units to use, 12=months, 1=days

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
            List<List<NumericPrediction>> forecast = forecaster.forecast(5, System.out); // ***** The number of predictions into the future
            // output the predictions. Outer list is over the steps; inner list is over the targets
            logger.warn("Confidence : " + forecaster.getConfidenceLevel());
            for (List<NumericPrediction> predictionsAtStep : forecast) {
                for (NumericPrediction predictionAtStep : predictionsAtStep) {
                    logger.warn("Prediction : " + predictionAtStep.predicted());
                }
            }
        }

        //noinspection unchecked
        return analysis;
    }

}