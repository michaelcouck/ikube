package ikube.use;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.analytics.weka.WekaToolkit;
import ikube.toolkit.CsvFileTools;
import ikube.toolkit.FileUtilities;
import org.junit.Ignore;
import org.junit.Test;
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
import static ikube.analytics.weka.WekaToolkit.writeToArff;
import static ikube.toolkit.FileUtilities.cleanFilePath;
import static ikube.toolkit.FileUtilities.findFileRecursively;
import static ikube.toolkit.FileUtilities.getOrCreateFile;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-09-2014
 */
@Ignore
public class ClickThroughTest extends AbstractTest {

    @Test
    public void regression() throws Exception {
        String[] args = new String[]{
                "-t", "8", // Threads
                "-f", "0", // Folds
                "-o", "regression", // Type
                "-n", "click-through-8-9", // Name and file
                "-e", "25", // Percentage permutations
                "-r", "[0]" // Reduce by columns
        };
        new ClickThrough(args);
    }

    @Test
    public void filterInstances() throws Exception {
        File file = findFileRecursively(new File("."), "click-through.csv");
        Object[][] matrix = new CsvFileTools().getCsvData(new FileInputStream(file));
        Instances instances = matrixToInstances(matrix, 0, String.class);
        instances = filter(instances, new StringToWordVector(), new NumericToNominal());

        File outputTrainingFile = getOrCreateFile(new File(System.nanoTime() + ".arff"));
        String outputTrainingFilePath = cleanFilePath(outputTrainingFile.getAbsolutePath());
        writeToArff(instances, outputTrainingFilePath);

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