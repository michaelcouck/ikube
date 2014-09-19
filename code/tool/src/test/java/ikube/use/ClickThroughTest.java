package ikube.use;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.toolkit.CsvFileTools;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.FileInputStream;

import static ikube.analytics.weka.WekaToolkit.filter;
import static ikube.analytics.weka.WekaToolkit.matrixToInstances;
import static ikube.toolkit.FileUtilities.findFileRecursively;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-09-2014
 */
@Ignore
public class ClickThroughTest extends AbstractTest {

    @Spy
    @InjectMocks
    private ClickThrough clickThrough;

    @Test
    public void hillClimb() throws Exception {
        clickThrough.regression();
    }

    @Test
    public void filterInstances() throws Exception {
        File file = findFileRecursively(new File("."), "click-through.csv");
        Object[][] matrix = new CsvFileTools().getCsvData(new FileInputStream(file));
        Instances instances = matrixToInstances(matrix, 0, String.class);
        instances = filter(instances, new StringToWordVector(), new NumericToNominal());

        instances.setClassIndex(0);

        Classifier classifier = new SMO();
        classifier.buildClassifier(instances);

        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            classifier.classifyInstance(instance);
            double[] distribution = classifier.distributionForInstance(instance);
            logger.error("Distribution : " + IConstants.GSON.toJson(distribution));
        }
    }

}