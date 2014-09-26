package ikube.analytics.weka;

import ikube.toolkit.MatrixUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.timeseries.WekaForecaster;
import weka.core.Attribute;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ikube.analytics.weka.WekaToolkit.*;
import static ikube.toolkit.CsvUtilities.getCsvData;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-09-2014
 */
public class WekaTimeSeriesClassifier {

    @SuppressWarnings("UnnecessaryLocalVariable")
    public void predictTimeSeries() throws Exception {
        // Get the data for the general trend in the sector type
        // Get the data for the general trend in all the sectors
        // Get the data for the general trend in the overall market

        // Add the trends for the sector to the stock type
        // Add the trends for the other sectors to the stock type
        // Add the trends for the market to the stock type

        String filePath = "/home/laptop/Downloads/table.csv";
        Object[][] matrix = getCsvData(filePath);
        MatrixUtilities.sortOnFeature(matrix, 0, Date.class);

        ArrayList<Attribute> attributes = new ArrayList<>();

        attributes.add(getAttribute(0, Date.class));
        attributes.add(getAttribute(1, Double.class));
        attributes.add(getAttribute(2, Double.class));
        attributes.add(getAttribute(3, Double.class));
        attributes.add(getAttribute(4, Double.class));
        attributes.add(getAttribute(5, Double.class));
        attributes.add(getAttribute(6, Double.class));

        Instances instances = new Instances("instances", attributes, attributes.size());
        for (final Object[] vector : matrix) {
            instances.add(getInstance(instances, vector));
        }
        instances.setClassIndex(6);

        writeToArff(instances, "target/table-filtered.arff");

        WekaForecaster forecaster = new WekaForecaster();
        forecaster.setFieldsToForecast("6");

        forecaster.getTSLagMaker().setTimeStampField("0"); // date time stamp
        forecaster.getTSLagMaker().setMinLag(1);
        forecaster.getTSLagMaker().setMaxLag(1); // 12 - monthly data

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
        List<List<NumericPrediction>> forecast = forecaster.forecast(5, System.out);
        // output the predictions. Outer list is over the steps; inner list is over the targets
        for (List<NumericPrediction> predsAtStep : forecast) {
            for (NumericPrediction predForTarget : predsAtStep) {
                System.out.print("" + predForTarget.predicted() + " ");
            }
            System.out.println();
        }
        System.out.println("Confidence : " + forecaster.getConfidenceLevel());
        Classifier classifier = forecaster.getBaseForecaster();
        double relativeAbsoluteError = WekaToolkit.crossValidate(classifier, instances, 3);
        System.out.println("Relative absolute error : " + relativeAbsoluteError);
    }

}