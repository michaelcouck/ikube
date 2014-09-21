package ikube.anal;

import ikube.AbstractTest;
import ikube.analytics.weka.WekaToolkit;
import ikube.toolkit.CsvFileTools;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.MatrixUtilities;
import org.junit.Ignore;
import org.junit.Test;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.*;

import java.io.File;

/**
 * TODO: Complete this test if necessary.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 16-09-2014
 */
@Ignore
public class AnalyzerClientTest extends AbstractTest {

    @Test
    public void create() throws Exception {
        String[] args = getArgs("create");
        AnalyzerClient.main(args);
    }

    @Test
    public void build() throws Exception {
        String[] args = getArgs("build");
        AnalyzerClient.main(args);
    }

    @Test
    public void analyze() throws Exception {
        String[] args = getArgs("analyze");
        AnalyzerClient.main(args);
    }

    @Test
    @Ignore
    public void invertTraining() throws Exception {
        // Get the matrix data
        File file = FileUtilities.findFileRecursively(new File("."), "stock.csv");
        String filePath = FileUtilities.cleanFilePath(file.getAbsolutePath());
        String[] args = {"-i", filePath};
        Object[][] data = CsvFileTools.getCsvData(args);
        Object[][] invertedData = MatrixUtilities.invertMatrix(data);

        // Create the instances from the matrix data
        FastVector attributes = new FastVector();
        for (int i = 0; i < invertedData[0].length; i++) {
            attributes.addElement(new Attribute(Integer.toHexString(i)));
        }

        Instances instances = new Instances("instances", attributes, 0);
        instances.setClass(instances.attribute(instances.numAttributes() - 1));
        // Populate the instances
        for (final Object[] row : invertedData) {
            double[] doubleRow = MatrixUtilities.objectVectorToDoubleVector(row);
            SparseInstance sparseInstance = new SparseInstance(1.0, doubleRow);
            instances.add(sparseInstance);
        }

        File outputFile = FileUtilities.getOrCreateFile("target/stock-instances.arff");
        String outputFilePath = FileUtilities.cleanFilePath(outputFile.getAbsolutePath());
        WekaToolkit.writeToArff(instances, outputFilePath);

        // Build the instances
        Classifier classifier = new LinearRegression();
        classifier.buildClassifier(instances);
        // Test the instances
        for (final Object[] invertedRow : invertedData) {
            double[] doubleRow = MatrixUtilities.objectVectorToDoubleVector(invertedRow);
            double[] doubleRowMissing = new double[doubleRow.length - 1];
            System.arraycopy(doubleRow, 0, doubleRowMissing, 0, doubleRowMissing.length - 1);
            Instance instance = new Instance(1.0, doubleRowMissing);
            // instance.setMissing(doubleRow.length - 1);
            instance.setDataset(instances);
            double index = classifier.classifyInstance(instance);
            logger.error("Index : " + index);
        }
    }

    String[] getArgs(final String method) {
        File inputFile = FileUtilities.findFileRecursively(new File("."), "stock.arff");
        String inputFilePath = FileUtilities.cleanFilePath(inputFile.getAbsolutePath());
        return new String[]{
                "-p", "80", "-h", "ikube.be", "-f", inputFilePath, "-op", method,
                "-n", "stock",
                "-i", "17.219,50.5,18.75,43,60.875,26.375,67.75,19,48.75,34.875",
                "-an", "ikube.analytics.weka.WekaClassifier",
                "-al", "weka.classifiers.functions.LinearRegression"};
    }

}