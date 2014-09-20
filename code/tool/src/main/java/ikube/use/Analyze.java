package ikube.use;

import ikube.analytics.weka.WekaToolkit;
import ikube.toolkit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static ikube.analytics.weka.WekaToolkit.filter;
import static ikube.analytics.weka.WekaToolkit.matrixToInstances;
import static ikube.toolkit.FileUtilities.findFileRecursively;
import static ikube.toolkit.FileUtilities.findFilesRecursively;
import static ikube.toolkit.ThreadUtilities.submit;
import static ikube.toolkit.ThreadUtilities.waitForFutures;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-09-2014
 */
public class Analyze {

    private static final Logger LOGGER = LoggerFactory.getLogger(Analyze.class);

    public static void main(final String[] args) throws Exception {
        ThreadUtilities.initialize();
        final Analyze analyze = new Analyze();
        CsvFileTools csvFileTools = new CsvFileTools();
        int[] excludedColumns = {0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
        List<File> trainingFiles = findFilesRecursively(new File("."), new ArrayList<File>(), "click-through-0-1");
        Filter[] filters = {new StringToWordVector(), new NumericToNominal() /* , new ReplaceMissingValues() */};

        List<Future<Object>> futures = new ArrayList<>();

        List<SMO> classifiers = new ArrayList<>();
        List<Instances> trainingInstancesList = new ArrayList<>();
        for (final File trainingFile : trainingFiles) {
            LOGGER.error("Training file : " + trainingFile);
            final SMO classifier = new SMO();
            Object[][] trainingMatrix = csvFileTools.getCsvData(new FileInputStream(trainingFile));
            final Instances trainingInstances = analyze.instances(trainingMatrix, excludedColumns, filters);

            trainingInstancesList.add(trainingInstances);
            File outputTrainingFile = FileUtilities.getOrCreateFile(new File(System.currentTimeMillis() + ".arff"));
            String outputTrainingFilePath = FileUtilities.cleanFilePath(outputTrainingFile.getAbsolutePath());
            WekaToolkit.writeToArff(trainingInstances, outputTrainingFilePath);
            @SuppressWarnings("unchecked")
            Future future = submit("click-through", new Runnable() {
                @Override
                public void run() {
                    try {
                        LOGGER.error(trainingInstances.numAttributes() + ":" + trainingInstances.numInstances());
                        analyze.build(classifier, trainingInstances);
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

        File testFile = findFileRecursively(new File("."), "test-1000.csv");
        Object[][] testMatrix = csvFileTools.getCsvData(new FileInputStream(testFile));
        // Instances testInstances = analyze.instances(testMatrix, excludedColumns, filters);
        List<Double> probabilityForInstances = new ArrayList<>();
        for (final Instances trainingInstances : trainingInstancesList) {
            for (int i = 0; i < trainingInstances.numInstances(); i++) {
                double probability = 0;
                Instance instance = trainingInstances.instance(i);
                for (int j = 0; j < classifiers.size(); j++) {
                    SMO classifier = classifiers.get(j);
                    Instances instances = trainingInstancesList.get(j);
                    instance.setDataset(instances);
                    classifier.classifyInstance(instance);
                    double[] distributionForInstance = classifier.distributionForInstance(instance);
                    probability += distributionForInstance[1];
                }
                double averageProbability = probability / classifiers.size();
                probabilityForInstances.add(averageProbability);
            }
            File outputFile = FileUtilities.getOrCreateFile(new File("test-results.csv"));
            FileWriter fileWriter = new FileWriter(outputFile);
            int index = 60000000;
            for (final double probability : probabilityForInstances) {
                fileWriter.write(index++);
                fileWriter.write(",");
                fileWriter.write(Double.toString(probability));
                fileWriter.write("\n");
            }
        }
    }

    private Instances instances(final Object[][] matrix, final int[] excludedColumns, final Filter... filters) throws Exception {
        MatrixUtilities.excludeColumns(matrix, excludedColumns);
        Instances instances = matrixToInstances(matrix, 0, Double.class);
        return filter(instances, filters);
    }

    private void build(final SMO classifier, final Instances filteredInstances) throws Exception {
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                try {
                    classifier.turnChecksOff();
                    // classifier.setBuildLogisticModels(Boolean.TRUE);
                    classifier.buildClassifier(filteredInstances);
                    LOGGER.error("Built classifier : " + classifier);
                } catch (final Exception e) {
                    LOGGER.error("Exception building classifier : ", e);
                }
            }
        });
        LOGGER.error("Built classifier in : " + duration);
    }

    private int[][] analyze(final Classifier classifier, final Instances testInstances) throws Exception {
        for (int i = 0; i < testInstances.numInstances(); i++) {
            Instance instance = testInstances.instance(i);
            double classification = classifier.classifyInstance(instance);
            double[] distributionForInstance = classifier.distributionForInstance(instance);
            LOGGER.error("Classification : " + classification + ", actual : " + instance.toDoubleArray()[0]);
            for (final double probability : distributionForInstance) {
                LOGGER.error("        distribution : " + probability);
            }
        }
        return null;
    }

}