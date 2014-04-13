package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08-09-2013
 */
public class WekaClassifierTest extends AbstractTest {

    private String positive = "my beautiful little girl";
    private Context<WekaClassifier, StringToWordVector, SMO, ?> context;
    /**
     * Class under test
     */
    private WekaClassifier wekaClassifier;

    @Before
    public void before() throws Exception {
        context = new Context<>();
        context.setAlgorithm(SMO.class.newInstance());
        context.setFilter(StringToWordVector.class.newInstance());
        context.setName("sentiment-smo-en-test");
        context.setMaxTraining(10000);

        wekaClassifier = new WekaClassifier() {
            void persist(final Context context, final Instances instances) {
                // Do nothing
            }
        };
        wekaClassifier.init(context);
        wekaClassifier.build(context);
    }

    @Test
    public void init() throws Exception {
        SMO smo = mock(SMO.class);
        Context context = mock(Context.class);
        when(context.getName()).thenReturn("sentiment-smo-en-test");
        when(context.getAlgorithm()).thenReturn(smo);
        when(context.getOptions()).thenReturn(new String[]{"-R", "8"});
        wekaClassifier.init(context);
        verify(context, atLeastOnce()).getAlgorithm();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
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
        Object algorithm = context.getAnalyzer();
        wekaClassifier.init(context);
        Instances instances = Deencapsulation.getField(wekaClassifier, "instances");
        context.setMaxTraining(instances.numInstances());
        wekaClassifier.build(context);
        // TODO: For now we don't clean the data model after building
        // assertEquals("The instances are cleaned after the build : ", 0, instances.numInstances());
        assertNotSame("The classifier algorithm must be replaced with the built one : ", algorithm, context.getAlgorithm());
    }

    @Test
    public void analyze() throws Exception {
        wekaClassifier.init(context);
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
        Instances instances = Deencapsulation.getField(wekaClassifier, "instances");
        Instance instance = instances.firstInstance();

        double classOrCluster = wekaClassifier.classOrCluster(instance);
        assertEquals(0.0, classOrCluster);

        instance = instances.instance(2188);
        classOrCluster = wekaClassifier.classOrCluster(instance);
        assertEquals(1.0, classOrCluster);
    }

    @Test
    public void distributionForInstance() throws Exception {
        Instances instances = Deencapsulation.getField(wekaClassifier, "instances");

        Instance instance = instances.firstInstance();

        double[] distributionForInstance = wekaClassifier.distributionForInstance(instance);
        assertEquals(1.0, distributionForInstance[0]);
        assertEquals(0.0, distributionForInstance[1]);

        instance = instances.instance(2188);

        distributionForInstance = wekaClassifier.distributionForInstance(instance);
        assertEquals(0.0, distributionForInstance[0]);
        assertEquals(1.0, distributionForInstance[1]);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void multiThreaded() throws Exception {
        ThreadUtilities.initialize();
        final int iterations = 10;
        List<Future<Object>> futures = new ArrayList<>();
        final AtomicInteger exceptions = new AtomicInteger(0);

        Future<Object> future = (Future<Object>) ThreadUtilities.submit(Long.toString(System.currentTimeMillis()), new Runnable() {
            public void run() {
                int i = iterations;
                while (--i > 0) {
                    try {
                        wekaClassifier.build(context);
                        ThreadUtilities.sleep(3000);
                    } catch (final Exception e) {
                        exceptions.incrementAndGet();
                    }
                }
            }
        });
        futures.add(future);
        future = (Future<Object>) ThreadUtilities.submit(Long.toString(System.currentTimeMillis()), new Runnable() {
            public void run() {
                int i = iterations * 100;
                while (--i > 0) {
                    try {
                        Analysis<String, double[]> analysis = getAnalysis(IConstants.POSITIVE, positive);
                        wekaClassifier.train(analysis);
                        ThreadUtilities.sleep(1000);
                    } catch (Exception e) {
                        exceptions.incrementAndGet();
                    }
                }
            }
        });
        futures.add(future);
        future = (Future<Object>) ThreadUtilities.submit(Long.toString(System.currentTimeMillis()), new Runnable() {
            public void run() {
                int i = iterations * 1000;
                while (--i > 0) {
                    try {
                        Analysis<String, double[]> analysis = getAnalysis(IConstants.POSITIVE, positive);
                        wekaClassifier.analyze(analysis);
                    } catch (Exception e) {
                        exceptions.incrementAndGet();
                    }
                }
            }
        });
        futures.add(future);

        ThreadUtilities.waitForFutures(futures, 15);
        assertEquals(0, exceptions.intValue());
    }

    @Test
    public void size() throws Exception {
        Analysis<String, double[]> analysis = getAnalysis(IConstants.POSITIVE, null);
        int sizeForClass = wekaClassifier.sizeForClassOrCluster(analysis);
        assertTrue(sizeForClass > 20);
    }

    @Test
    public void classes() throws Exception {
        Object[] classes = wekaClassifier.classesOrClusters();
        assertEquals(2, classes.length);
        assertEquals(IConstants.POSITIVE, classes[0]);
        assertEquals(IConstants.NEGATIVE, classes[1]);
    }

}