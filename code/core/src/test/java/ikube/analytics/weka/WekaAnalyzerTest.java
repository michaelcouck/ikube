package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2013
 */
public class WekaAnalyzerTest extends AbstractTest {

    private Context context;
    private Analysis analysis;
    private WekaAnalyzer wekaAnalyzer;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        ThreadUtilities.initialize();
        wekaAnalyzer = new WekaClassifier();

        String algorithm = SMO.class.getName();
        String filter = StringToWordVector.class.getName();
        String[] options = new String[]{"-V", "100"};
        int maxTraining = 10000;

        context = new Context();
        context.setName("classification");
        context.setAnalyzer(wekaAnalyzer);

        context.setAlgorithms(algorithm, algorithm, algorithm);
        context.setFilters(filter, filter, filter);
        context.setOptions(options);

        context.setFileNames("sentiment-smo-one.arff", "sentiment-smo-two.arff", "sentiment-smo-three.arff");
        context.setMaxTrainings(maxTraining, maxTraining, maxTraining);

        analysis = new Analysis();
    }

    @After
    public void after() {
        Mockit.tearDownMocks(WekaToolkit.class);
    }

    @Test
    public void init() throws Exception {
        wekaAnalyzer.init(context);
        for (int i = 0; i < context.getAlgorithms().length; i++) {
            assertTrue(SMO.class.isAssignableFrom(context.getAlgorithms()[i].getClass()));
            assertTrue(Filter.class.isAssignableFrom(context.getFilters()[i].getClass()));
            assertTrue(Instances.class.isAssignableFrom(context.getModels()[i].getClass()));
        }
        wekaAnalyzer.build(context);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void sizeForClassOrCluster() throws Exception {
        init();
        analysis.setClazz(IConstants.POSITIVE);
        int sizeForClass = wekaAnalyzer.sizeForClassOrCluster(context, analysis);
        assertEquals(9, sizeForClass);
    }

    @Test
    public void destroy() throws Exception {
        init();
        for (final Object model : context.getModels()) {
            assertEquals(7, ((Instances) model).numInstances());
        }
        wekaAnalyzer.destroy(context);
        for (final Object model : context.getModels()) {
            assertEquals(0, ((Instances) model).numInstances());
        }
    }

    @Test
    public void instance() throws Exception {
        init();
        Instances instances = mock(Instances.class);
        Attribute attTwo = new Attribute("one", (List<String>) null, 1);
        Attribute attThree = new Attribute("two", (List<String>) null, 2);

        when(instances.numAttributes()).thenReturn(3);
        when(instances.attribute(1)).thenReturn(attTwo);
        when(instances.attribute(2)).thenReturn(attThree);

        String input = "my beautiful little girl";
        Instance instance = wekaAnalyzer.instance(input + ", " + input, instances);
        assertEquals(instances, instance.dataset());
        assertEquals(3, instance.numAttributes());
        assertEquals(3, instance.numValues());

        assertEquals(attTwo, instance.attribute(1));
        assertEquals(attThree, instance.attribute(2));
    }

    @Test
    public void instances() throws Exception {
        init();
        Instances[] models = wekaAnalyzer.instances(context);
        for (final Instances instances : models) {
            assertNotNull(instances);
            assertEquals(2, instances.numAttributes());
            assertEquals(7, instances.numInstances());
            assertEquals(Attribute.NOMINAL, instances.attribute(0).type());
            assertEquals(Attribute.STRING, instances.attribute(1).type());
        }
    }

    @Test
    public void getInputStream() throws Exception {
        wekaAnalyzer.init(context);
        wekaAnalyzer.build(context);
        InputStream[] inputStreams = wekaAnalyzer.getInputStreams(context);
        assertTrue(inputStreams.length > 0);
        for (final InputStream inputStream : inputStreams) {
            assertNotNull(inputStream);
        }
    }

    @Test
    public void filterInstance() throws Exception {
        init();
        Object[] filters = context.getFilters();
        Instances[] models = wekaAnalyzer.instances(context);
        for (int i = 0; i < filters.length; i++) {
            Filter filter = (Filter) filters[i];
            Instances instances = models[i];
            filter.setInputFormat(instances);
            Filter.useFilter(instances, filter);

            Enumeration enumeration = instances.enumerateInstances();
            while (enumeration.hasMoreElements()) {
                Instance instance = (Instance) enumeration.nextElement();
                Instance filteredInstance = WekaToolkit.filter(instance, filter);
                assertNotSame(instance.toString(), filteredInstance.toString());
            }
        }
    }

    @Test
    public void filterInstances() throws Exception {
        init();
        Object[] filters = context.getFilters();
        Instances[] models = wekaAnalyzer.instances(context);
        for (int i = 0; i < filters.length; i++) {
            Filter filter = (Filter) filters[i];
            Instances instances = models[i];
            Instances filteredInstances = WekaToolkit.filter(instances, filter);
            assertNotSame(instances.firstInstance().toString(), filteredInstances.firstInstance().toString());
        }
    }

    @Test
    public void getDistributionForInstances() throws Exception {
        init();
        Instances[] models = wekaAnalyzer.instances(context);
        for (final Instances instances : models) {
            double[][][] distributionForInstances = wekaAnalyzer.getDistributionForInstances(context, instances);
            for (final double[][] distributionForInstance : distributionForInstances) {
                for (final double[] distributionInstance : distributionForInstance) {
                    for (final double probability : distributionInstance) {
                        assertTrue(probability == 0.0 || probability == 1.0);
                    }
                }
            }
        }
    }

}