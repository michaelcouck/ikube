package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.model.Context;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21.11.13
 */
public class WekaAnalyzerTest extends AbstractTest {

    private Context context;
    private WekaClassifier wekaClassifier;

    @Before
    public void before() throws Exception {
        context = new Context();
        wekaClassifier = new WekaClassifier();
        context.setName("classification");
        context.setFilter(StringToWordVector.class.newInstance());
        context.setAlgorithm(SMO.class.newInstance());
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
        Instance instance = wekaClassifier.instance(input + ", " + input, instances);
        assertEquals(instances, instance.dataset());
        assertEquals(3, instance.numAttributes());
        assertEquals(3, instance.numValues());

        assertEquals(attTwo, instance.attribute(1));
        assertEquals(attThree, instance.attribute(2));
    }

    @Test
    public void instances() throws Exception {
        Instances instances = wekaClassifier.instances(context);
        assertNotNull(instances);
        assertEquals(2, instances.numAttributes());
        assertEquals(Attribute.NOMINAL, instances.attribute(0).type());
        assertEquals(Attribute.STRING, instances.attribute(1).type());
    }

}