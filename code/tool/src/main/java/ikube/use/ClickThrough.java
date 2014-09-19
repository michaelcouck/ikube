package ikube.use;

import com.google.common.util.concurrent.AtomicDouble;
import ikube.IConstants;
import ikube.toolkit.CsvFileTools;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.*;
import weka.classifiers.meta.AdditiveRegression;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Range;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

import static ikube.analytics.weka.WekaToolkit.filter;
import static ikube.analytics.weka.WekaToolkit.matrixToInstances;
import static org.springframework.util.ReflectionUtils.doWithMethods;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-09-2014
 */
@SuppressWarnings("FieldCanBeLocal")
public class ClickThrough {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClickThrough.class);

    @Option(name = "-n", usage = "The name of the analyzer and the file name prefix, click-through for example")
    private String name = "click-through";
    @Option(name = "-o", usage = "The name of the operation of method to call")
    private String operationName = "regression";
    @Option(name = "-t", usage = "The number of concurrent threads to use")
    private int threads = 4;
    /**
     * The matrix of data that will be manipulated to get the best algorithm.
     */
    private Object[][] matrix;

    public ClickThrough() throws CmdLineException {
        this(null);
    }

    public ClickThrough(final String[] args) throws CmdLineException {
        if (args != null) {
            CmdLineParser parser = new CmdLineParser(this);
            parser.setUsageWidth(140);
            parser.parseArgument(args);
        }

        ThreadUtilities.initialize();
        File file = FileUtilities.findFileRecursively(new File("."), name + ".csv");
        try {
            matrix = new CsvFileTools().getCsvData(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        final ClickThrough clickThrough = this;

        doWithMethods(this.getClass(), new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException {
                if (method.getName().equals(operationName)) {
                    try {
                        method.invoke(clickThrough);
                    } catch (final InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    public void regression() throws Exception {
        String[] classifiers = {
                IsotonicRegression.class.getName(),
                SimpleLinearRegression.class.getName(),
                AdditiveRegression.class.getName(),
                PaceRegression.class.getName(),
                LinearRegression.class.getName()
        };
        permuteVectorSpace(classifiers, matrix);
    }

    public void classification() throws Exception {
        String[] classifiers = {
                SMO.class.getName()
        };
        permuteVectorSpace(classifiers, matrix, new StringToWordVector(), new NumericToNominal());
    }

    @SuppressWarnings("unchecked")
    private void permuteVectorSpace(final String[] classifiers, final Object[][] matrix, final Filter... filters) throws Exception {
        List<Future<Object>> futures = new ArrayList<>();
        final AtomicDouble bestErrorRate = new AtomicDouble(Integer.MAX_VALUE);
        int[][] excludedColumnsPermutations = excludedColumnsPermutation(matrix);
        for (final String classifier : classifiers) {
            for (final int[] excludedColumns : excludedColumnsPermutations) {
                class Executor implements Runnable {
                    public void run() {
                        try {
                            Classifier instanceClassifier = (Classifier) Class.forName(classifier).newInstance();
                            double errorRate = analyzeForMatrixRegression(instanceClassifier, matrix, excludedColumns, filters);
                            if (errorRate < bestErrorRate.doubleValue()) {
                                bestErrorRate.set(errorRate);
                                LOGGER.error(classifier + " : " + IConstants.GSON.toJson(errorRate));
                                LOGGER.error(IConstants.GSON.toJson(excludedColumns));
                            }
                        } catch (final Exception e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
                Future<?> future = ThreadUtilities.submit(this.toString(), new Executor());
                futures.add((Future<Object>) future);
                if (futures.size() == threads) {
                    ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
                    futures.clear();
                }
            }
        }
        ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
    }

    private int[][] excludedColumnsPermutation(final Object[][] matrix) {
        List<int[]> excludedColumnsList = new ArrayList<>();
        int length = matrix[0].length - 20;
        // Create an initial vector/set
        ICombinatoricsVector<Integer> initialSet = Factory.createVector();
        for (int i = 0; i < length; i++) {
            initialSet.addValue(matrix[0].length - length + i);
        }
        // Create an instance of the subset generator
        Generator<Integer> generator = Factory.createSubSetGenerator(initialSet);
        // Print the subsets
        for (ICombinatoricsVector<Integer> subSet : generator) {
            int[] subExcludedColumns = new int[subSet.getSize()];
            for (int i = 0; i < subSet.getSize(); i++) {
                subExcludedColumns[i] = subSet.getValue(i);
            }
            excludedColumnsList.add(subExcludedColumns);
        }
        int[][] excludedColumnsArray = new int[excludedColumnsList.size()][];
        for (int i = 0; i < excludedColumnsArray.length; i++) {
            excludedColumnsArray[i] = excludedColumnsList.get(i);
        }
        return excludedColumnsArray;
    }

    private double analyzeForMatrixRegression(final Classifier classifier, final Object[][] data, final int[] excludedColumns, final Filter... filters) throws Exception {
        Instances instances = matrixToInstances(data, 0, Double.class, excludedColumns);
        instances = filter(instances, filters);
        classifier.buildClassifier(instances);
        return crossValidate(classifier, instances, 3);
    }

    private double crossValidate(final Classifier classifier, final Instances instances, final int folds) throws Exception {
        Evaluation evaluation = new Evaluation(instances);
        StringBuffer predictionsOutput = new StringBuffer();
        evaluation.crossValidateModel(classifier, instances, folds, new Random(), predictionsOutput, new Range(), true);
        evaluation.evaluateModel(classifier, instances);
        if (LOGGER.isDebugEnabled()) {
            String summary = evaluation.toSummaryString();
            LOGGER.error("Classifier : " + classifier.getClass().getName());
            LOGGER.error("           : " + predictionsOutput);
            LOGGER.error("           : " + evaluation.pctCorrect());
            LOGGER.error("           : " + evaluation.pctIncorrect());
            LOGGER.error("           : " + evaluation.pctUnclassified());
            LOGGER.error("           : " + summary);
        }
        return evaluation.relativeAbsoluteError();
    }

    private int analyzeForInstances(final Instances instances, final Classifier classifier) throws Exception {
        int hits = 0;
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            double classification = classifier.classifyInstance(instance);
            double[] distributionForInstance = classifier.distributionForInstance(instance);
            LOGGER.error("Classification : " + classification + ", actual : " + instance.toDoubleArray()[0]);
            for (final double probability : distributionForInstance) {
                LOGGER.error("        distribution : " + probability);
            }
        }
        return hits;
    }

}