package ikube.analytics;

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
	public void init() throws Exception {
		wekaClassifier.init(buildable);
	}

	@Test
	public void train() throws Exception {
		wekaClassifier.train(IConstants.POSITIVE, input);
	}

	@Test
	public void build() throws Exception {
		wekaClassifier.build(buildable);
	}

	@Test
	public void analyze() throws Exception {
		String result = wekaClassifier.analyze(input);
		logger.info("Result : " + result);
	}

}