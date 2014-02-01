package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Analysis;
import ikube.model.Context;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08.09.13
 */
public class WekaClassifierTest extends AbstractTest {

    private String positive = "my beautiful little girl";
    private Context<WekaClassifier, StringToWordVector, SMO> context;
    /**
     * Class under test
     */
    private WekaClassifier wekaClassifier;

    @Before
    public void before() throws Exception {
        context = new Context<>();
        context.setAlgorithm(SMO.class.newInstance());
        context.setFilter(StringToWordVector.class.newInstance());
        context.setName("sentiment-en");
        context.setMaxTraining(1000);

        wekaClassifier = new WekaClassifier();
    }

    @Test
    public void init() throws Exception {
        Context context = mock(Context.class);
        when(context.getName()).thenReturn("sentiment-en");
        wekaClassifier.init(context);
        verify(context, atLeastOnce()).getAlgorithm();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        wekaClassifier.init(context);
        Instances instances = Deencapsulation.getField(wekaClassifier, "instances");
        int initial = instances.numInstances();
        int iterations = context.getMaxTraining();
        do {
            Analysis<String, double[]> analysis = getAnalysis(IConstants.POSITIVE, positive);
            boolean trained = wekaClassifier.train(analysis);
            assertTrue(trained);
        } while (--iterations >= 0);
        assertEquals(context.getMaxTraining() + initial + 1, instances.numInstances());
    }

    @Test
    public void build() throws Exception {
        wekaClassifier.init(context);
        Instances instances = Deencapsulation.getField(wekaClassifier, "instances");
        context.setMaxTraining(instances.numInstances());
        wekaClassifier.build(context);
        assertEquals(0, instances.numInstances());
    }

    @Test
    public void analyze() throws Exception {
        wekaClassifier.init(context);
        // String negative = "you selfish stupid woman";
        String negative = "narryontop harry styles hello harry";
        Analysis<String, double[]> analysis = getAnalysis(IConstants.NEGATIVE, negative);
        wekaClassifier.train(analysis);
        wekaClassifier.build(context);

        analysis = getAnalysis(null, positive);
        Analysis<String, double[]> result = wekaClassifier.analyze(analysis);
        assertEquals(IConstants.POSITIVE, result.getClazz());

        analysis = getAnalysis(null, negative);
        result = wekaClassifier.analyze(analysis);
        assertEquals(IConstants.NEGATIVE, result.getClazz());

        /*System.gc();
        long before = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / IConstants.MILLION;
        for (int i = 0; i < IConstants.HUNDRED_THOUSAND; i++) {
            wekaClassifier.analyze(analysis);
        }
        System.gc();
        long after = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / IConstants.MILLION;
        logger.info("Before : " + before + ", " + after + ", " + (after - before));*/
    }

    @Test
    public void classOrCluster() throws Exception {
        wekaClassifier.init(context);
        wekaClassifier.build(context);

        Instances instances = Deencapsulation.getField(wekaClassifier, "instances");
        Instance instance = instances.firstInstance();

        double classOrCluster = wekaClassifier.classOrCluster(instance);
        assertEquals(0.0, classOrCluster);

        instance = instances.instance(2);
        classOrCluster = wekaClassifier.classOrCluster(instance);
        assertEquals(1.0, classOrCluster);
    }

    @Test
    public void distributionForInstance() throws Exception {
        wekaClassifier.init(context);
        wekaClassifier.build(context);

        Instances instances = Deencapsulation.getField(wekaClassifier, "instances");
        Instance instance = instances.firstInstance();

        double[] distributionForInstance = wekaClassifier.distributionForInstance(instance);
        assertEquals(0.6666666666666666, distributionForInstance[0]);
        assertEquals(0.3333333333333333, distributionForInstance[1]);
        assertEquals(0.0, distributionForInstance[2]);
    }

}