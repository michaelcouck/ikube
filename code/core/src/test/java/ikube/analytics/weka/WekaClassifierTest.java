package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Analysis;
import ikube.model.Context;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.Arrays;
import java.util.Enumeration;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08-09-2013
 */
public class WekaClassifierTest extends AbstractTest {

    private String positive = "my beautiful little girl";

    @Mock
    private Context context;
    @Spy
    @InjectMocks
    private WekaClassifier wekaClassifier;

    @Before
    public void before() throws Exception {
        String algorithm = SMO.class.getName();
        String filter = StringToWordVector.class.getName();
        String fileName = "sentiment-smo.arff";
        String[] options = new String[]{"-D", "-V", "100"};
        int maxTraining = 10000;

        context = new Context();
        context.setName("classification");
        context.setAlgorithms(algorithm, algorithm, algorithm);
        context.setFilters(filter, filter, filter);
        context.setOptions(options);
        context.setFileNames(fileName, fileName, fileName);
        context.setMaxTrainings(maxTraining, maxTraining, maxTraining);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(wekaClassifier).persist(any(Context.class));

        wekaClassifier.init(context);
    }

    @Test
    public void build() throws Exception {
        wekaClassifier.build(context);
        assertEquals(3, context.getEvaluations().length);
        for (final String evaluation : context.getEvaluations()) {
            logger.error("Evaluation : " + evaluation);
            assertNotNull(evaluation);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        Instances[] instanceses = (Instances[]) context.getModels();
        for (final Instances instances : instanceses) {
            int iterations = IConstants.HUNDRED_THOUSAND;
            int numInstances = instances.numInstances();
            do {
                Analysis<Object, Object> analysis = getAnalysis(IConstants.POSITIVE, positive);
                boolean trained = wekaClassifier.train(context, analysis);
                assertTrue(trained);
            } while (--iterations >= 0);
            assertEquals(IConstants.HUNDRED_THOUSAND + numInstances + 1, instances.numInstances());
        }
    }

    @Test
    public void analyze() throws Exception {
        String negative = "narryontop harry styles hello harry";
        Analysis<Object, Object> analysis = getAnalysis(IConstants.NEGATIVE, negative);
        wekaClassifier.train(context, analysis);
        wekaClassifier.build(context);

        analysis = getAnalysis(null, positive);
        Analysis<Object, Object> result = wekaClassifier.analyze(context, analysis);
        assertEquals(IConstants.POSITIVE, result.getClazz());

        analysis = getAnalysis(null, negative);
        result = wekaClassifier.analyze(context, analysis);
        assertEquals(IConstants.NEGATIVE, result.getClazz());

        System.gc();
        long before = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / IConstants.MILLION;
        for (int i = 0; i < IConstants.HUNDRED_THOUSAND; i++) {
            analysis = getAnalysis(null, positive);
            analysis = wekaClassifier.analyze(context, analysis);
            assertEquals(IConstants.POSITIVE, analysis.getClazz());
            analysis = getAnalysis(null, negative);
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