package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.MatrixUtilities;
import junit.framework.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import weka.core.Attribute;
import weka.core.Instances;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static ikube.analytics.weka.WekaToolkit.getAttribute;
import static ikube.analytics.weka.WekaToolkit.getInstance;
import static ikube.toolkit.CsvUtilities.getCsvData;
import static ikube.toolkit.FileUtilities.findFileAndGetCleanedPath;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-09-2014
 */
public class WekaForecastClassifierTest extends AbstractTest {

    @Spy
    @InjectMocks
    private WekaForecastClassifier wekaForecastClassifier;

    @Test
    public void analyze() throws Exception {
        Instances instances = getInstances();

        Context context = new Context();
        context.setModels(instances);

        Analysis analysis = new Analysis();
        //noinspection unchecked
        analysis.setInput(new String[]{
                "-fieldsToForecast", "6",
                "-timeStampField", "0",
                "-minLag", "1",
                "-maxLag", "1",
                "-forecasts", "5"
        });

        wekaForecastClassifier.analyze(context, analysis);

        double[][][] modelPredictions = (double[][][]) analysis.getOutput();
        Assert.assertEquals(1, modelPredictions.length);
        for (final double[][] timePrediction : modelPredictions) {
            for (final double[] fieldPrediction : timePrediction) {
                logger.error("Prediction : " + Arrays.toString(fieldPrediction));
                for (final double prediction : fieldPrediction) {
                    Assert.assertTrue(prediction > 570 && prediction < 590);
                }
            }
        }
    }

    private Instances getInstances() {
        String filePath = findFileAndGetCleanedPath(new File("."), "forecast-stock-data.csv");
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
        // instances.setClassIndex(6);
        return instances;
    }

}