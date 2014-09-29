package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.Arrays;
import java.util.Enumeration;

import static junit.framework.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08-09-2013
 */
public class WekaClassifierTest extends AbstractTest {

    private String positive = "my beautiful little girl";
    @SuppressWarnings("FieldCanBeLocal")
    private String negative = "I hate this selfish woman";

    @Mock
    private Context context;
    @Spy
    @InjectMocks
    private WekaClassifier wekaClassifier;

    @Before
    public void before() throws Exception {
        ThreadUtilities.initialize();

        String algorithm = NaiveBayesMultinomial.class.getName();
        String filter = StringToWordVector.class.getName();
        String fileName = "sentiment-smo.arff";
        // "-V", "100"
        String[] options = new String[]{"-D"};
        int maxTraining = 10000;

        context = new Context();
        context.setName("classification");
        context.setAnalyzer(WekaClassifier.class.getName());
        context.setAlgorithms(algorithm, algorithm, algorithm);
        context.setFilters(filter);
        context.setOptions(options);
        context.setFileNames(fileName, fileName, fileName);
        context.setMaxTrainings(maxTraining, maxTraining, maxTraining);

        wekaClassifier.init(context);
    }

    @Test
    public void build() throws Exception {
        wekaClassifier.build(context);
        ThreadUtilities.sleep(1000);
        assertEquals(3, context.getEvaluations().length);
        for (final String evaluation : context.getEvaluations()) {
            logger.error("Evaluation : " + evaluation);
            assertNotNull(evaluation);
        }

        context.setBuilt(Boolean.FALSE);
        context.setPersisted(Boolean.FALSE);

        wekaClassifier.build(context);
        ThreadUtilities.sleep(1000);
        assertTrue(context.isBuilt());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        Instances[] instancesArray = (Instances[]) context.getModels();
        for (final Instances instances : instancesArray) {
            int iterations = IConstants.ONE_THOUSAND;
            int numInstances = instances.numInstances();
            do {
                Analysis<Object, Object> analysis = getAnalysis(IConstants.POSITIVE, positive + "," + positive);
                boolean trained = wekaClassifier.train(context, analysis);
                assertTrue(trained);
            } while (--iterations >= 0);
            assertEquals(IConstants.ONE_THOUSAND + numInstances + 1, instances.numInstances());
        }
    }

    @Test
    public void analyze() throws Exception {
        Analysis<Object, Object> analysis = getAnalysis(IConstants.NEGATIVE, IConstants.NEGATIVE + "," + negative);
        wekaClassifier.train(context, analysis);
        wekaClassifier.build(context);

        analysis = getAnalysis(null, "?," + positive);
        Analysis<Object, Object> result = wekaClassifier.analyze(context, analysis);
        assertEquals(IConstants.POSITIVE, result.getClazz());

        analysis = getAnalysis(null, "?," + negative);
        result = wekaClassifier.analyze(context, analysis);
        assertEquals(IConstants.NEGATIVE, result.getClazz());

        System.gc();
        long before = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / IConstants.MILLION;
        for (int i = 0; i < IConstants.ONE_THOUSAND; i++) {
            analysis = getAnalysis(null, "?," + positive);
            analysis = wekaClassifier.analyze(context, analysis);
            assertEquals(IConstants.POSITIVE, analysis.getClazz());
            analysis = getAnalysis(null, "?," + negative);
            analysis = wekaClassifier.analyze(context, analysis);
            assertEquals(IConstants.NEGATIVE, analysis.getClazz());
        }
        System.gc();
        long after = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / IConstants.MILLION;
        logger.warn("Before : " + before + ", after : " + after + ", difference : " + (after - before));
    }

    @Test
    public void distributionForInstance() throws Exception {
        wekaClassifier.build(context);
        ThreadUtilities.sleep(1000);
        for (final Object model : context.getModels()) {
            Instances instances = (Instances) model;
            Enumeration instanceEnumeration = instances.enumerateInstances();
            while (instanceEnumeration.hasMoreElements()) {
                Instance instance = (Instance) instanceEnumeration.nextElement();
                double[][] distributionForInstance = wekaClassifier.distributionForInstance(context, instance);
                assertTrue(Arrays.equals(distributionForInstance[0], distributionForInstance[1]));
                assertTrue(Arrays.equals(distributionForInstance[1], distributionForInstance[2]));
                for (double[] aDistributionForInstance : distributionForInstance) {
                    assertNotSame(aDistributionForInstance[0], aDistributionForInstance[1]);
                }
            }
        }
    }

}