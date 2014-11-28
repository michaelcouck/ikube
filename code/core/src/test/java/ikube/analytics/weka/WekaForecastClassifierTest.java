package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.MatrixUtilities;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import weka.core.Attribute;
import weka.core.Instances;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static ikube.analytics.weka.WekaToolkit.getAttribute;
import static ikube.analytics.weka.WekaToolkit.getInstance;
import static ikube.toolkit.CSV.getCsvData;
import static ikube.toolkit.FILE.findFileAndGetCleanedPath;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-09-2014
 */
public class WekaForecastClassifierTest extends AbstractTest {

    @Mock
    private Context context;
    @Spy
    @InjectMocks
    private WekaForecastClassifier wekaForecastClassifier;

    @Test
    public void init() {
        Context context = getContext();
        wekaForecastClassifier.init(context);
        verify(context, times(1)).setModels(any());
    }

    @Test
    public void analyze() throws Exception {
        context = getContext();

        Analysis analysis = new Analysis();
        //noinspection unchecked
        analysis.setInput(getOptions());

        wekaForecastClassifier.analyze(context, analysis);

        double[][][] modelPredictions = (double[][][]) analysis.getOutput();
        assertEquals(1, modelPredictions.length);
        for (final double[][] timePrediction : modelPredictions) {
            for (final double[] fieldPrediction : timePrediction) {
                logger.info("Prediction : " + Arrays.toString(fieldPrediction));
                for (final double prediction : fieldPrediction) {
                    assertTrue(prediction > 570 && prediction < 590);
                }
            }
        }

        //noinspection unchecked
        analysis.setInput("-fieldsToForecast,6,-timeStampField,0,-minLag,1,-maxLag,1,-forecasts,5");
        wekaForecastClassifier.analyze(context, analysis);
    }

    protected Context getContext() {
        Instances instances = getInstances();
        when(context.getName()).thenReturn("forecast-classifier");
        when(context.getAnalyzer()).thenReturn(WekaForecastClassifier.class.getName());
        when(context.getOptions()).thenReturn(getOptions());
        when(context.getModels()).thenReturn(new Instances[]{instances});
        return context;
    }

    private Object[] getOptions() {
        return new String[]{
                "-fieldsToForecast", "6",
                "-timeStampField", "0",
                "-minLag", "1",
                "-maxLag", "1",
                "-forecasts", "5"
        };
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
        return instances;
    }

}