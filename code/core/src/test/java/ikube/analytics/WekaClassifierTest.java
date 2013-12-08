package ikube.analytics;

import static junit.framework.Assert.assertEquals;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Analysis;
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
	private IAnalyzer<Analysis<String, double[]>, Analysis<String, double[]>> wekaClassifier;

	private Buildable buildable;
	private String positive = "my beautiful little girl";
	private String negative = "you selfish stupid woman";

	@Before
	public void before() throws Exception {
		buildable = new Buildable();
		buildable.setLog(false);
		buildable.setAlgorithmType(SMO.class.getName());
		buildable.setFilterType(StringToWordVector.class.getName());
		buildable.setTrainingFilePath("src/test/resources/analytics/classification.arff");

		wekaClassifier = new WekaClassifier();
	}

	@Test
	public void init() throws Exception {
		wekaClassifier.init(buildable);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void train() throws Exception {
		wekaClassifier.init(buildable);
		int iterations = 1000 + 1;
		do {
			Analysis<String, double[]> analysis = getAnalysis(IConstants.POSITIVE, positive);
			wekaClassifier.train(analysis);
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

		Analysis<String, double[]> analysis = getAnalysis(null, positive);
		Analysis<String, double[]> result = wekaClassifier.analyze(analysis);
		assertEquals(IConstants.POSITIVE, result.getClazz());

		analysis = getAnalysis(null, negative);
		result = wekaClassifier.analyze(analysis);
		assertEquals(IConstants.NEGATIVE, result.getClazz());
	}

}