package ikube.analytics;

import static junit.framework.Assert.assertEquals;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Buildable;

import org.junit.Before;
import org.junit.Test;

import weka.classifiers.functions.SMO;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * @author Michael Couck
 * @since 08.09.13
 * @version 01.00
 */
public class WekaClassifierTest extends AbstractTest {

	/** Class under test */
	private IAnalyzer<String, String> wekaClassifier;

	private Buildable buildable;
	private String positive = "my beautiful little girl";
	private String negative = "you selfish stupid woman";

	@Before
	public void before() {
		buildable = new Buildable();
		buildable.setLog(false);
		buildable.setAlgorithmType(SMO.class.getName());
		buildable.setFilterType(StringToWordVector.class.getName());
		buildable.setTrainingFilePath("src/test/resources/analytics/instance.arff");

		wekaClassifier = new WekaClassifier();
	}

	@Test
	public void init() throws Exception {
		wekaClassifier.init(buildable);
	}

	@Test
	public void train() throws Exception {
		wekaClassifier.init(buildable);
		int iterations = WekaClassifier.BUILD_THRESHOLD + 1;
		do {
			wekaClassifier.train(IConstants.POSITIVE, positive);
		} while (iterations-- >= 0);
	}

	@Test
	public void build() throws Exception {
		wekaClassifier.init(buildable);
		wekaClassifier.build(buildable);
	}

	@Test
	public void analyze() throws Exception {
		wekaClassifier.init(buildable);
		wekaClassifier.build(buildable);
		String result = wekaClassifier.analyze(positive);
		assertEquals(IConstants.POSITIVE, result);
		result = wekaClassifier.analyze(negative);
		assertEquals(IConstants.NEGATIVE, result);
	}

}