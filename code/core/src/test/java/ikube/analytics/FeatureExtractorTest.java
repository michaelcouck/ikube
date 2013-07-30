package ikube.analytics;

import ikube.AbstractTest;

import java.io.IOException;
import java.util.Arrays;

import libsvm.LibSVM;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

import org.junit.Before;
import org.junit.Test;

public class FeatureExtractorTest extends AbstractTest {

	private FeatureExtractor featureExtractor;

	@Before
	public void before() {
		featureExtractor = new FeatureExtractor();
	}

	@Test
	public void extractFeatures() throws IOException {
		String dictionaryTerms = "this wonderful world love all you need shit what a lousy day";
		double[] positiveVector = featureExtractor.extractFeatures("this wonderful world love is all you need", dictionaryTerms);
		Instance positiveInstance = new DenseInstance(positiveVector, "positive");

		double[] negativeVector = featureExtractor.extractFeatures("shit what a lousy day"); // new double[] { 0, 0, 0, 0 };
		Instance negativeInstance = new DenseInstance(negativeVector, "negative");

		Dataset dataset = new DefaultDataset(Arrays.asList(positiveInstance, negativeInstance));

		LibSVM libSvmClassifier = new LibSVM();
		libSvmClassifier.buildClassifier(dataset);

		double[] toClassifyVector = featureExtractor.extractFeatures("love");
		Instance toClassifyInstance = new DenseInstance(toClassifyVector);
		Object result = libSvmClassifier.classify(toClassifyInstance);
		logger.info("Result : " + result);

		toClassifyVector = featureExtractor.extractFeatures("shit");
		toClassifyInstance = new DenseInstance(toClassifyVector);
		result = libSvmClassifier.classify(toClassifyInstance);
		logger.info("Result : " + result);
	}

}
