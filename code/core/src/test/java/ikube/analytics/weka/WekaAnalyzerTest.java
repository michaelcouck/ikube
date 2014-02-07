package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Analysis;
import ikube.model.Context;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.InputStream;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21.11.13
 */
public class WekaAnalyzerTest extends AbstractTest {

    static boolean WRITTEN = Boolean.FALSE;

    @SuppressWarnings("UnusedDeclaration")
    @MockClass(realClass = WekaToolkit.class)
    public static class WekaToolkitMock {
        @Mock
        public static void writeToArff(final Instances instances, final String filePath) {
            WRITTEN = Boolean.TRUE;
        }
    }

    private Context context;
    private WekaAnalyzer wekaAnalyzer;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        context = new Context();
        context.setName("sentiment-en");
        context.setFilter(StringToWordVector.class.newInstance());
        context.setAlgorithm(SMO.class.newInstance());
        context.setMaxTraining(1000);

        wekaAnalyzer = new WekaClassifier();
        wekaAnalyzer.init(context);
        wekaAnalyzer.build(context);
        Mockit.setUpMocks(WekaToolkitMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(WekaToolkit.class);
    }

    @Test
    public void instance() throws Exception {
        Instances instances = mock(Instances.class);
        Attribute attTwo = new Attribute("one", (FastVector) null, 1);
        Attribute attThree = new Attribute("two", (FastVector) null, 2);

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
        Instances instances = wekaAnalyzer.instances(context);
        assertNotNull(instances);
        assertEquals(2, instances.numAttributes());
        assertEquals(Attribute.NOMINAL, instances.attribute(0).type());
        assertEquals(Attribute.STRING, instances.attribute(1).type());
    }

    @Test
    public void persist() throws Exception {
        WRITTEN = Boolean.FALSE;
        Instances instances = wekaAnalyzer.instances(context);
        wekaAnalyzer.persist(context, instances);
        assertTrue(WRITTEN);
    }

    @Test
    public void getInputStream() throws Exception {
        InputStream inputStream = wekaAnalyzer.getInputStream(context);
        assertNotNull(inputStream);
    }

    @Test
    public void getDataFile() throws Exception {
        File dataFile = wekaAnalyzer.getDataFile(context);
        assertNotNull(dataFile);
    }

    @Test
    public void filterInstance() throws Exception {
        Filter filter = (Filter) context.getFilter();
        Instances instances = wekaAnalyzer.instances(context);
        Instance instance = instances.firstInstance();

        filter.setInputFormat(instances);
        Filter.useFilter(instances, filter);

        Instance filteredInstance = wekaAnalyzer.filter(instance, filter);
        assertNotSame(instance.toString(), filteredInstance.toString());
    }

    @Test
    public void filterInstances() throws Exception {
        Filter filter = (Filter) context.getFilter();
        Instances instances = wekaAnalyzer.instances(context);
        Instances filteredInstances = wekaAnalyzer.filter(instances, filter);
        assertNotSame(instances.firstInstance().toString(), filteredInstances.firstInstance().toString());
    }

    @Test
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    public void getCorrelationCoefficients() throws Exception {
        Instances instances = wekaAnalyzer.instances(context);
        double[] correlationCoefficients = wekaAnalyzer.getCorrelationCoefficients(instances);
        for (final double correlationCoefficient : correlationCoefficients) {
            assertTrue(correlationCoefficient >= -1.0 && correlationCoefficient <= 1.0);
        }
    }

    @Test
    public void getDistributionForInstances() throws Exception {
        Instances instances = wekaAnalyzer.instances(context);
        double[][] distributionForInstances = wekaAnalyzer.getDistributionForInstances(instances);
        for (final double[] distributionForInstance : distributionForInstances) {
            for (final double probability : distributionForInstance) {
                assertTrue(probability >= 0.0 || probability <= 1.0);
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void size() throws Exception {
        Analysis analysis = getAnalysis(IConstants.POSITIVE, null);
        int sizeForClazz = wekaAnalyzer.sizeForClassOrCluster(analysis);
        assertEquals(529, sizeForClazz);
    }

}