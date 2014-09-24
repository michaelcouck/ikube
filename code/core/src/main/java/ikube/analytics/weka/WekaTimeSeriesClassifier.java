package ikube.analytics.weka;

import ikube.toolkit.MatrixUtilities;
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
        String filePath = "/home/laptop/Downloads/netflix/processed-data/mv-aggregated-1411559729692-1000.csv";
        Object[][] matrix = getCsvData(filePath);
        MatrixUtilities.sortOnFeature(matrix, 5, Date.class);

        ArrayList<Attribute> attributes = new ArrayList<>();

        attributes.add(getAttribute(0, Double.class));
        attributes.add(getAttribute(1, Double.class));
        attributes.add(getAttribute(2, String.class));
        attributes.add(getAttribute(3, Double.class));
        attributes.add(getAttribute(4, Double.class));
        attributes.add(getAttribute(5, Date.class));

        Instances instances = new Instances("instances", attributes, 0);
        for (final Object[] vector : matrix) {
            instances.add(getInstance(instances, vector));
        }

        Instances filteredInstances = instances; // filter(instances, new StringToWordVector());
        writeToArff(filteredInstances, "target/mv-aggregated-filtered.arff");

        WekaForecaster forecaster = new WekaForecaster();
        forecaster.setFieldsToForecast("4");

        forecaster.getTSLagMaker().setTimeStampField("5"); // date time stamp
        forecaster.getTSLagMaker().setMinLag(1);
        forecaster.getTSLagMaker().setMaxLag(12); // monthly data

        // add a month of the year indicator field
        forecaster.getTSLagMaker().setAddMonthOfYear(true);

        // add a quarter of the year indicator field
        forecaster.getTSLagMaker().setAddQuarterOfYear(true);

        // build the model
        forecaster.buildForecaster(filteredInstances, System.out);

        // prime the forecaster with enough recent historical data
        // to cover up to the maximum lag. In our case, we could just supply
        // the 12 most recent historical instances, as this covers our maximum
        // lag period
        forecaster.primeForecaster(filteredInstances);

        // forecast for 12 units (months) beyond the end of the
        // training data
        List<List<NumericPrediction>> forecast = forecaster.forecast(12, System.out);

        // output the predictions. Outer list is over the steps; inner list is over
        // the targets
        for (int i = 0; i < 12; i++) {
            List<NumericPrediction> predsAtStep = forecast.get(i);
            for (int j = 0; j < 2; j++) {
                NumericPrediction predForTarget = predsAtStep.get(j);
                System.out.print("" + predForTarget.predicted() + " ");
            }
            System.out.println();
        }
    }

}
