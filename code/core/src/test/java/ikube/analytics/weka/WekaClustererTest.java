package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Analysis;
import ikube.model.Context;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Arrays;

import static junit.framework.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 14-11-2013
 */
public class WekaClustererTest extends AbstractTest {

    private Context context;
    @Spy
    @InjectMocks
    private WekaClusterer wekaClusterer;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {

        String algorithm = EM.class.getName();
        String[] options = new String[]{"-N", "6"};
        String fileName = "bmw-browsers.arff";
        int maxTraining = Integer.MAX_VALUE;

        context = new Context();
        context.setName("clusterer");
        context.setAnalyzer(WekaClusterer.class.getName());
        context.setAlgorithms(algorithm, algorithm, algorithm);
        context.setOptions(options);
        context.setFileNames(fileName, fileName, fileName);
        context.setMaxTrainings(maxTraining, maxTraining, maxTraining);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(wekaClusterer).persist(any(Context.class));

        wekaClusterer.init(context);
    }

    @Test
    public void build() throws Exception {
        wekaClusterer.build(context);
        assertEquals(3, context.getEvaluations().length);
        for (final String evaluation : context.getEvaluations()) {
            logger.error("Evaluation : " + evaluation);
            assertNotNull(evaluation);
        }
    }

    @Test
    public void analyze() throws Exception {
        wekaClusterer.build(context);
        Analysis<Object, Object> analysis = getAnalysis(null, "1,0,1,1,1,1,1,1");

        Analysis<Object, Object> result = wekaClusterer.analyze(context, analysis);
        logger.error(IConstants.GSON.toJson(result.getOutput()));
        logger.error(IConstants.GSON.toJson(result.getClazz()));
        for (final String evaluation : context.getEvaluations()) {
            logger.error(evaluation);
        }
    }

    @Test
    public void getDistributionForInstances() throws Exception {
        wekaClusterer.build(context);
        for (final Object model : context.getModels()) {
            Instances instances = (Instances) model;
            double[][][] distributionForInstances = wekaClusterer.getDistributionForInstances(context, instances);
            for (final double[][] distributionForInstance : distributionForInstances) {
                logger.info("Probability : " + Arrays.toString(distributionForInstance));
                for (final double[] distribution : distributionForInstance) {
                    for (final double probability : distribution) {
                        assertTrue(probability >= 0 && probability <= 1.0);
                    }
                }
            }
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private void printInstances(final Instances instances) {
        logger.info("Num attributes : " + instances.numAttributes());
        logger.info("Num instances : " + instances.numInstances());
        logger.info("Sum of weights : " + instances.sumOfWeights());
        for (int i = 0; i < instances.numAttributes(); i++) {
            Attribute attribute = instances.attribute(i);
            logger.info("Attribute : " + attribute);
        }
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            logger.info("Instance : " + instance);
        }
    }

}