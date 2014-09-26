package ikube.analytics.weka;

import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.MatrixUtilities;
import org.junit.Test;
import weka.core.Attribute;
import weka.core.Instances;

import java.io.File;
import java.util.ArrayList;
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
public class WekaForecastClassifierTest {

    @Test
    public void predictTimeSeries() throws Exception {
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

        Context context = new Context();
        Analysis analysis = new Analysis();
        context.setModels(instances);

        new WekaForecastClassifier().analyze(context, analysis);
    }

}
