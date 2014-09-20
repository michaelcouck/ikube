package ikube.use;

import com.google.common.util.concurrent.AtomicDouble;
import ikube.analytics.weka.WekaToolkit;
import ikube.toolkit.CsvFileTools;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.MatrixUtilities;
import ikube.toolkit.ThreadUtilities;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.IsotonicRegression;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.PaceRegression;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.classifiers.meta.AdditiveRegression;
import weka.core.Instances;
import weka.core.Range;
import weka.filters.Filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

import static ikube.Constants.GSON;
import static ikube.analytics.weka.WekaToolkit.filter;
import static ikube.analytics.weka.WekaToolkit.matrixToInstances;
import static ikube.toolkit.PerformanceTester.APerform;
import static ikube.toolkit.PerformanceTester.execute;
import static ikube.toolkit.ThreadUtilities.submit;
import static ikube.toolkit.ThreadUtilities.waitForFutures;
import static org.springframework.util.ReflectionUtils.MethodCallback;
import static org.springframework.util.ReflectionUtils.doWithMethods;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-09-2014
 */
@SuppressWarnings("FieldCanBeLocal")
public class ClickThrough {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClickThrough.class);

    public static void main(final String[] args) throws CmdLineException {
        new ClickThrough(args);
    }

    @Option(name = "-n", usage = "The name of the analyzer and the file name prefix, " +
            "click-through for example would mean the file so use is click-through.csv")
    private String name = "click-through";
    @Option(name = "-o", usage = "The name of the operation of method to call, at the time of writing there were " +
            "two methods, classification and regression. Classification using the normal classifiers, and regression using the " +
            "regression algorithms")
    private String operationName = "regression";
    @Option(name = "-t", usage = "The number of concurrent threads to use in parallel to do the analysis")
    private int threads = 1;
    @Option(name = "-f", usage = "The number of cross validation folds to use for the validation and training")
    private int folds = 0;
    @Option(name = "-e", usage = "The percentage of excluded columns to cycle through to find the best error rate")
    private int excludedColumnsPercentage = 10;
    @SuppressWarnings("UnusedDeclaration")
    @Option(name = "-r", usage = "The columns that must be removed from the matrix to reduce the dimensionality, must be in Json array format, i.e.")
    private String reduceDimensionalityByColumns;
    /**
     * The matrix of data that will be manipulated to get the best algorithm.
     */
    private Object[][] matrix;

    public ClickThrough() throws CmdLineException {
        this(null);
    }

    public ClickThrough(final String[] args) throws CmdLineException {
        init(args);
    }

    void init(final String[] args) throws CmdLineException {
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

        doWithMethods(this.getClass(), new MethodCallback() {
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

    @SuppressWarnings("unchecked")
    private void permuteVectorSpace(final String[] classifiers, final Object[][] matrix, final Filter... filters) throws Exception {
        List<Future<Object>> futures = new ArrayList<>();
        final AtomicDouble bestErrorRate = new AtomicDouble(Integer.MAX_VALUE);
        final Object[][] reducedMatrix;
        if (reduceDimensionalityByColumns == null) {
            reducedMatrix = matrix;
        } else {
            reducedMatrix = MatrixUtilities.excludeColumns(matrix, GSON.fromJson(reduceDimensionalityByColumns, int[].class));
        }
        int[][] excludedColumnsPermutations = excludedColumnsPermutation(reducedMatrix);
        for (final int[] excludedColumns : excludedColumnsPermutations) {
            final Instances instances = instances(matrix, excludedColumns, filters);
            for (final String classifier : classifiers) {
                class Executor implements Runnable {
                    public void run() {
                        try {
                            Classifier instanceClassifier = (Classifier) Class.forName(classifier).newInstance();
                            instanceClassifier.buildClassifier(instances);
                            double errorRate = crossValidate(instanceClassifier, instances, folds);
                            if (errorRate < bestErrorRate.doubleValue()) {
                                bestErrorRate.set(errorRate);
                                LOGGER.error(classifier + " : " + GSON.toJson(errorRate) + " : " + GSON.toJson(excludedColumns));
                            }
                            LOGGER.error("      : " + classifier + " : " + GSON.toJson(errorRate) + " : " + GSON.toJson(excludedColumns));
                        } catch (final Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                Future<?> future = submit(this.toString(), new Executor());
                futures.add((Future<Object>) future);
                if (futures.size() == threads) {
                    waitForFutures(futures, Integer.MAX_VALUE);
                    futures.clear();
                }
            }
        }
        LOGGER.error("Waiting for futures : " + futures.size());
        waitForFutures(futures, Integer.MAX_VALUE);
    }


    private Instances instances(final Object[][] matrix, final int[] excludedColumns, final Filter... filters) throws Exception {
        Object[][] prunedMatrix = MatrixUtilities.excludeColumns(matrix, excludedColumns);
        Instances instances = matrixToInstances(prunedMatrix, 0, Double.class);
        File outputTrainingFile = FileUtilities.getOrCreateFile(new File(System.nanoTime() + ".arff"));
        String outputTrainingFilePath = FileUtilities.cleanFilePath(outputTrainingFile.getAbsolutePath());
        WekaToolkit.writeToArff(instances, outputTrainingFilePath);
        return filter(instances, filters);
    }

    private double crossValidate(final Classifier classifier, final Instances instances, final int folds) throws Exception {
        final Evaluation evaluation = new Evaluation(instances);
        final StringBuffer predictionsOutput = new StringBuffer();
        if (folds > 2) {
            execute(new APerform() {
                public void execute() throws Throwable {
                    evaluation.crossValidateModel(classifier, instances, folds, new Random(), predictionsOutput, new Range(), true);
                }
            }, "Duration for cross validation : ", 1, true);

        }
        execute(new APerform() {
            public void execute() throws Throwable {
                evaluation.evaluateModel(classifier, instances);
            }
        }, "Duration for model evaluation : ", 1, true);

//        String summary = evaluation.toSummaryString();
//        LOGGER.error("Classifier : " + classifier.getClass().getName());
//        LOGGER.error("           : " + predictionsOutput);
//        LOGGER.error("           : " + evaluation.pctCorrect());
//        LOGGER.error("           : " + evaluation.pctIncorrect());
//        LOGGER.error("           : " + evaluation.pctUnclassified());
//        LOGGER.error("           : " + summary);

        return evaluation.relativeAbsoluteError();
    }

    private int[][] excludedColumnsPermutation(final Object[][] matrix) {
        List<int[]> excludedColumnsList = new ArrayList<>();
        int lengthOfExcludedColumnsArray = matrix[0].length * excludedColumnsPercentage / 100;
        // Create an initial vector/set
        ICombinatoricsVector<Integer> initialSet = Factory.createVector();
        for (int i = 0; i < lengthOfExcludedColumnsArray; i++) {
            initialSet.addValue(matrix[0].length - lengthOfExcludedColumnsArray + i);
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
        LOGGER.error("Excluded columns dimension : " + excludedColumnsArray.length);
        return excludedColumnsArray;
    }

}