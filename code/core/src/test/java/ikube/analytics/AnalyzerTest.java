package ikube.analytics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.model.Buildable;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.FastVector;
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
		buildable.setFilePath("src/test/resources/analytics/classification.arff");
		buildable.setFilter(StringToWordVector.class.getName());
		buildable.setLog(true);
		buildable.setType(SMO.class.getName());
	}

	@Test
	@Ignore
	public void instance() throws Exception {
		Instances instances = mock(Instances.class);
		Attribute attTwo = new Attribute("one", (FastVector) null);
		Attribute attThree = new Attribute("two", (FastVector) null);
		
//		when(attTwo.type()).thenReturn(Attribute.NOMINAL);
//		when(attThree.type()).thenReturn(Attribute.STRING);

		when(instances.numAttributes()).thenReturn(3);
		when(instances.attribute(1)).thenReturn(attTwo);
		when(instances.attribute(2)).thenReturn(attThree);

		wekaClassifier.instance(input + ", " + input, instances);
	}

	@Test
	public void instances() throws Exception {
		wekaClassifier.instances(buildable);
	}

}