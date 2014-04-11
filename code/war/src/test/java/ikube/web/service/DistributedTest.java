package ikube.web.service;

import ikube.BaseTest;
import ikube.analytics.AnalyzerManager;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.FileUtilities;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 24-02-2014
 */
public class DistributedTest extends BaseTest {

    private String analysisFolder = "./analysis-data";

    @Before
    public void before() {
        FileUtilities.deleteFile(new File(analysisFolder));
    }

    @After
    public void after() {
        FileUtilities.deleteFile(new File(analysisFolder));
    }

    @SuppressWarnings("unchecked")
    void partitionWith(
            final String name,
            final Class<? extends IAnalyzer> type,
            final Class<?> algorithm,
            final String header,
            final int pivot)
            throws Exception {
        logger.info("Algorithm : " + algorithm.getClass().getName());
        String input = "Benim güzel küçük kız, Tamara.";
        IAnalyzer analyzer = getAnalyzer(name, type, algorithm, null);
        Analysis analysis = getAnalysis(name, input);
        Analysis<String, double[]> result = (Analysis<String, double[]>) analyzer.analyze(analysis);
        double[] distributionForInstance = result.getOutput();
        logger.info("Distribution : " + Arrays.toString(distributionForInstance));

        File base = FileUtilities.findFileRecursively(new File("."), name + ".csv");
        List<String> lines = IOUtils.readLines(new FileInputStream(base));

        List<String> oneLines = new ArrayList<>();
        List<String> twoLines = new ArrayList<>();

        oneLines.add(header);
        twoLines.add(header);

        int index = 0;
        double[][] distributionForInstances = result.getDistributionForInstances();
        for (final double[] d : distributionForInstances) {
            double biggest = 0;
            int biggestIndex = 0;
            for (int i = 0; i < d.length; i++) {
                if (d[i] >= biggest) {
                    biggest = d[i];
                    biggestIndex = i;
                }
            }
            if (biggestIndex < pivot) {
                oneLines.add(lines.get(index));
            } else {
                twoLines.add(lines.get(index));
            }
            index++;
            // logger.info(Arrays.toString(d));
        }

        String nameOne = name + "-one";
        String nameTwo = name + "-two";
        File oneFile = FileUtilities.getOrCreateFile(analysisFolder + "/" + nameOne + ".arff");
        File twoFile = FileUtilities.getOrCreateFile(analysisFolder + "/" + nameTwo + ".arff");

        IOUtils.writeLines(oneLines, "\n", new FileOutputStream(oneFile));
        IOUtils.writeLines(twoLines, "\n", new FileOutputStream(twoFile));

        analyzer = getAnalyzer(nameOne, type, algorithm, null);
        analysis = getAnalysis(nameOne, input);
        result = (Analysis<String, double[]>) analyzer.analyze(analysis);
        double[] distributionForInstanceOne = result.getOutput();
        logger.info("One : " + Arrays.toString(distributionForInstanceOne));

        analyzer = getAnalyzer(nameTwo, type, algorithm, null);
        analysis = getAnalysis(nameTwo, input);
        result = (Analysis<String, double[]>) analyzer.analyze(analysis);
        double[] distributionForInstanceTwo = result.getOutput();
        logger.info("Two : " + Arrays.toString(distributionForInstanceTwo));

        logger.info(Arrays.toString(distributionForInstance));
        logger.info(Arrays.toString(distributionForInstanceOne) + Arrays.toString(distributionForInstanceTwo));
    }

    IAnalyzer getAnalyzer(
            final String analyzerName,
            final Class<? extends IAnalyzer> type,
            final Class<?> algorithm,
            final String[] options)
            throws Exception {
        Context context = getContext(analyzerName, type, algorithm, options);
        return AnalyzerManager.buildAnalyzer(context, Boolean.TRUE);
    }

    @SuppressWarnings("unchecked")
    Context getContext(
            final String name,
            final Class<? extends IAnalyzer> type,
            final Class<?> algorithm,
            final String[] options)
            throws Exception {
        Context context = new Context();
        context.setName(name);
        context.setAnalyzer(type.newInstance());
        context.setAlgorithm(algorithm.newInstance());
        context.setFilter(StringToWordVector.class.newInstance());
        context.setOptions(options);
        context.setMaxTraining(1000);
        return context;
    }

    Analysis getAnalysis(final String analyzer, final String input) {
        Analysis<String, double[]> analysis = new Analysis<>();
        analysis.setAnalyzer(analyzer);
        analysis.setInput(input);
        analysis.setDistribution(Boolean.TRUE);
        analysis.setClassesAndClusters(Boolean.TRUE);
        analysis.setAlgorithm(Boolean.TRUE);
        analysis.setCorrelation(Boolean.TRUE);
        return analysis;
    }

}
