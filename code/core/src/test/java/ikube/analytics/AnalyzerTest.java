package ikube.analytics;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.model.Buildable;

import org.junit.Before;
import org.junit.Test;

import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * @author Michael Couck
 * @since 21.11.13
 * @version 01.00
 */
public class AnalyzerTest extends AbstractTest {

	private Buildable buildable;
	private WekaClassifier wekaClassifier;
	private String input = "my beautiful little girl";

	@Before
	public void before() {
		buildable = new Buildable();
		wekaClassifier = new WekaClassifier();
		buildable.setTrainingFilePath("src/test/resources/analytics/classification.arff");
		buildable.setFilterType(StringToWordVector.class.getName());
		buildable.setLog(true);
		buildable.setAlgorithmType(SMO.class.getName());
	}

	@Test
	public void instance() throws Exception {
		Instances instances = mock(Instances.class);
		Attribute attTwo = new Attribute("one", (FastVector) null, 1);
		Attribute attThree = new Attribute("two", (FastVector) null, 2);

		when(instances.numAttributes()).thenReturn(3);
		when(instances.attribute(1)).thenReturn(attTwo);
		when(instances.attribute(2)).thenReturn(attThree);

		Instance instance = wekaClassifier.instance(input + ", " + input, instances);
		assertEquals(instances, instance.dataset());
		assertEquals(3, instance.numAttributes());
		assertEquals(3, instance.numValues());

		assertEquals(attTwo, instance.attribute(1));
		assertEquals(attThree, instance.attribute(2));
	}

	@Test
	public void instances() throws Exception {
		Instances instances = wekaClassifier.instances(buildable);
		assertNotNull(instances);
		assertEquals(2, instances.numAttributes());
		assertEquals(Attribute.NOMINAL, instances.attribute(0).type());
		assertEquals(Attribute.STRING, instances.attribute(1).type());
	}

}