package ikube.use;

import ikube.IConstants;
import ikube.analytics.IAnalyzer;
import ikube.analytics.weka.WekaClassifier;
import ikube.analytics.weka.WekaToolkit;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.CsvFileTools;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import static ikube.analytics.weka.WekaToolkit.matrixToInstances;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-09-2014
 */
public class ClickThrough {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClickThrough.class);

    @SuppressWarnings("unchecked")
    public void hillClimb() throws Exception {
        // Load the data from the file
        // Test several algorithms, varying the parameters
        // Retest the algorithms, modifying the instances, reducing the vector space, etc.
        String name = "click-through";
        File file = FileUtilities.findFileRecursively(new File("."), name + ".csv");

        Object[][] data = new CsvFileTools().getCsvData(new FileInputStream(file));

        ThreadUtilities.initialize();

        Context context = new Context();
        context.setName(name);
        context.setAnalyzer(WekaClassifier.class.getName());
        context.setAlgorithms(LinearRegression.class.getName());
        context.setFileNames(name + ".arff");
        context.setMaxTrainings(10000);

        IAnalyzer analyzer = new WekaClassifier();
        analyzer.init(context);
        analyzer.build(context);

        int excludedColumnsLength = data[0].length - 23;
        int[] excludedColumns = new int[excludedColumnsLength];
        for (int i = 0; i < excludedColumnsLength; i++) {
            excludedColumns[i] = i + excludedColumnsLength;
        }

        int bestHits = 0;
        for (int i = 0; i < (1 << excludedColumns.length); i++) {
            ArrayList<Integer> subset = new ArrayList<>();
            for (int j = 0; j < excludedColumns.length; j++) {
                boolean bitOn = ((i >> j) & 1) == 1;
                if (bitOn) { // bit j is on
                    subset.add(excludedColumns[j]);
                }
            }
            int[] subExcludedColumns = new int[subset.size()];
            for (int j = 0; j < subExcludedColumns.length; j++) {
                subExcludedColumns[j] = subset.get(j);
            }
            int hits = hillClimb(analyzer, context, data, subExcludedColumns);
            if (hits > bestHits) {
                LOGGER.error("Iteration : " + i + ", hits : " + hits + ", best hits : " + bestHits);
                bestHits = hits;
                LOGGER.error(subset.toString());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private int hillClimb(final IAnalyzer classifier, final Context context, final Object[][] data, final int... excludedColumns) throws Exception {
        File file = FileUtilities.getOrCreateFile("target/instances.arff");
        String filePath = FileUtilities.cleanFilePath(file.getAbsolutePath());
        Instances instances = matrixToInstances(data, 0, excludedColumns);
        WekaToolkit.writeToArff(instances, filePath);
        context.setModels(instances);
        classifier.build(context);
        return analyzeInstances(context, instances, classifier);
    }

    @SuppressWarnings("unchecked")
    private int analyzeInstances(final Context context, final Instances instances, final IAnalyzer analyzer) throws Exception {
        int hits = 0;
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            Analysis analysis = new Analysis();
            analysis.setContext(context.getName());
            analysis.setAddAlgorithmOutput(Boolean.TRUE);
            analysis.setInput(StringUtils.strip(IConstants.GSON.toJson(instance.toDoubleArray()), "[]"));

            Analysis result = (Analysis) analyzer.analyze(context, analysis);
            double[] output = (double[]) analysis.getOutput();
            double actual = instance.toDoubleArray()[0];
            if (output[0] > 0.5 && actual == 1.0) {
                hits++;
                // LOGGER.error("Result : " + result + ", output : " + IConstants.GSON.toJson(output) + ", actual : " + actual);
            }
        }
        return hits;
    }

}