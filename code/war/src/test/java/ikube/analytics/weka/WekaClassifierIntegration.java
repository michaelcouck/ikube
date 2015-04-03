package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.analytics.AnalyzerManager;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.functions.SMO;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-11-2014
 */
public class WekaClassifierIntegration extends AbstractTest {

    private Object[] algorithms;
    private String[] fileNames;
    private Object[] filters;
    private int[] maxTrainings;

    @Before
    public void before() {
        List<String> modelFiles = new ArrayList<>();

        modelFiles.add("xaa-s.arff");
        modelFiles.add("xab-s.arff");
        modelFiles.add("xac-s.arff");
        modelFiles.add("xad-s.arff");
        modelFiles.add("xae-s.arff");
        modelFiles.add("xaf-s.arff");
        modelFiles.add("xag-s.arff");
        modelFiles.add("xah-s.arff");
        modelFiles.add("xai-s.arff");

        algorithms = new Object[modelFiles.size()];
        filters = new Object[modelFiles.size()];
        maxTrainings = new int[modelFiles.size()];

        for (int i = 0; i < modelFiles.size(); i++) {
            algorithms[i] = SMO.class.getName();
            filters[i] = StringToWordVector.class.getName();
            maxTrainings[i] = 1000000;
        }

        fileNames = modelFiles.toArray(new String[modelFiles.size()]);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws Exception {
        Context context = new Context();
        context.setAnalyzer(WekaClassifier.class.getName());
        context.setAlgorithms(algorithms);
        context.setFileNames(fileNames);
        context.setFilters(filters);
        context.setMaxTrainings(maxTrainings);
        context.setBuildInParallel(Boolean.TRUE);
        // context.setOptions("-D");

        IAnalyzer analyzer = new AnalyzerManager().buildAnalyzer(context, Boolean.TRUE);
        Analysis analysis = new Analysis();
        analysis.setAddAlgorithmOutput(Boolean.TRUE);
        analysis.setContext(context.getName());
        analysis.setDistributed(Boolean.FALSE);

        analysis.setInput("i missed the new moon trailer");
        analysis = (Analysis) analyzer.analyze(context, analysis);
        logger.info("1) " + analysis.getClazz());
        assertEquals(IConstants.NEGATIVE, analysis.getClazz());

        analysis.setInput("omg its already o");
        analysis = (Analysis) analyzer.analyze(context, analysis);
        logger.info("2) " + analysis.getClazz());
        assertEquals(IConstants.POSITIVE, analysis.getClazz());

        analysis.setInput("or i just worry too much");
        analysis = (Analysis) analyzer.analyze(context, analysis);
        logger.info("3) " + analysis.getClazz());
        assertEquals(IConstants.NEGATIVE, analysis.getClazz());

        analysis.setInput("hmmmm i wonder how she my number");
        analysis = (Analysis) analyzer.analyze(context, analysis);
        logger.info("4) " + analysis.getClazz());
        assertEquals(IConstants.POSITIVE, analysis.getClazz());
    }

}