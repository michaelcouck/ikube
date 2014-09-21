package ikube.use;

import ikube.toolkit.CsvFileTools;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.Timer;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.RegressionByDiscretization;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Range;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

import static ikube.analytics.weka.WekaToolkit.filter;
import static ikube.analytics.weka.WekaToolkit.matrixToInstances;
import static ikube.toolkit.FileUtilities.*;
import static ikube.toolkit.MatrixUtilities.excludeColumns;
import static ikube.toolkit.ThreadUtilities.submit;
import static ikube.toolkit.ThreadUtilities.waitForFutures;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-09-2014
 */
@SuppressWarnings("FieldCanBeLocal")
public class Analyze {

    static {
        ThreadUtilities.initialize();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Analyze.class);

    public static void main(final String[] args) throws Exception {
        new Analyze().doMain(args);
    }

    @Option(name = "-n")
    private String name = "click-through";
    @Option(name = "-f")
    private int folds = 10;

    private int[] trainingExcludedFeatures;
    private int[] testExcludedFeatures;

    @Option(name = "-ev")
    private boolean evaluate = Boolean.TRUE;

    @Option(name = "-t")
    private String testFile = "test.csv";
    @Option(name = "-r")
    private String testResults = "test-results.csv";

    private void doMain(final String[] args) throws Exception {
        initParameters(args);
        Filter[] filters = {new ReplaceMissingValues()};

        Object[][][] matrices = initMatrices(name);
        List<Classifier> classifiers = build(matrices, trainingExcludedFeatures, filters);
        analyze(classifiers, testExcludedFeatures, filters);
    }

    private void initParameters(final String[] args) throws Exception {
        if (args != null) {
            CmdLineParser parser = new CmdLineParser(this);
            parser.setUsageWidth(140);
            parser.parseArgument(args);
            trainingExcludedFeatures = new int[]{0, 21, 30, 32, 33, 34, 35, 37};
            testExcludedFeatures = new int[]{20, 29, 31, 32, 33, 34, 36};
        }
    }

    private List<Classifier> build(final Object[][][] matrices, final int[] excludedColumns, final Filter... filters) throws Exception {
        List<Future<Object>> futures = new ArrayList<>();
        List<Classifier> classifiers = new ArrayList<>();

        for (final Object[][] trainingMatrix : matrices) {
            final Classifier classifier = new RegressionByDiscretization();
            final Instances trainingInstances = instances(trainingMatrix, excludedColumns, filters);
            @SuppressWarnings("unchecked")
            Future future = submit("click-through", new Runnable() {
                @Override
                public void run() {
                    try {
                        build(classifier, trainingInstances, folds);
                    } catch (final Exception e) {
                        LOGGER.error("Exception building classifier : ", e);
                    }
                }
            });
            //noinspection unchecked
            futures.add(future);
            classifiers.add(classifier);
        }
        waitForFutures(futures, Integer.MAX_VALUE);

        return classifiers;
    }

    private Object[][][] initMatrices(final String name) throws Exception {
        List<File> trainingFiles = findFilesRecursively(new File("."), new ArrayList<File>(), name);
        Object[][][] matrices = new Object[trainingFiles.size()][][];
        CsvFileTools csvFileTools = new CsvFileTools();
        for (int i = 0; i < trainingFiles.size(); i++) {
            File trainingFile = trainingFiles.get(i);
            LOGGER.error("Training file : " + trainingFile);
            FileInputStream fileInputStream = new FileInputStream(trainingFile);
            matrices[i] = csvFileTools.getCsvData(fileInputStream);
        }
        return matrices;
    }

    private void analyze(final List<Classifier> classifiers, final int[] excludedColumns, final Filter... filters) throws Exception {
        File testFile = findFileRecursively(new File("."), this.testFile);
        CsvFileTools csvFileTools = new CsvFileTools();
        Object[][] testMatrix = csvFileTools.getCsvData(new FileInputStream(testFile));
        Instances testInstances = instances(testMatrix, excludedColumns, filters);

        File outputFile = getOrCreateFile(new File(testResults));
        FileWriter fileWriter = new FileWriter(outputFile);

        for (int i = 0, index = 60000000, probability = 0; i < testInstances.numInstances(); i++, index++, probability = 0) {
            Instance instance = testInstances.instance(i);
            for (final Classifier classifier : classifiers) {
                instance.setDataset(testInstances);
                probability += classifier.classifyInstance(instance);
            }
            double averageProbability = probability / classifiers.size();
            // LOGGER.error("Probability : " + averageProbability);
            fileWriter.write(Integer.toString(index));
            fileWriter.write(",");
            fileWriter.write(Double.toString(averageProbability));
            fileWriter.write("\n");
        }
    }

    private Instances instances(final Object[][] matrix, final int[] excludedColumns, final Filter... filters) throws Exception {
        Object[][] prunedMatrix = excludeColumns(matrix, excludedColumns);
        LOGGER.error("Size : " + prunedMatrix[0].length);
        Instances instances = matrixToInstances(prunedMatrix, 0, Double.class);
        return filter(instances, filters);
    }

    private void build(final Classifier classifier, final Instances instances, final int folds) throws Exception {
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                try {
                    classifier.buildClassifier(instances);
                } catch (final Exception e) {
                    LOGGER.error("Exception building classifier : ", e);
                }
            }
        });
        LOGGER.error("Built classifier in : " + duration);

        duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                try {
                    if (folds > 0) {
                        Evaluation evaluation = new Evaluation(instances);
                        StringBuffer predictionsOutput = new StringBuffer();
                        evaluation.crossValidateModel(classifier, instances, folds, new Random(), predictionsOutput, new Range(), true);
                        LOGGER.error("Error : " + evaluation.relativeAbsoluteError() + ", " + classifier.getClass().getSimpleName());
                    }
                } catch (final Exception e) {
                    LOGGER.error("Exception building classifier : ", e);
                }
            }
        });

        LOGGER.error("Cross validated classifier in : " + duration);

        duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                try {
                    if (evaluate) {
                        Evaluation evaluation = new Evaluation(instances);
                        double[] result = evaluation.evaluateModel(classifier, instances);
                    }
                } catch (final Exception e) {
                    LOGGER.error("Exception building classifier : ", e);
                }
            }
        });

        LOGGER.error("Evaluated classifier in : " + duration);
    }

}