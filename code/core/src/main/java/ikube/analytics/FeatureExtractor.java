package ikube.analytics;

import ikube.toolkit.Logging;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import libsvm.LibSVM;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermFreqVector;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.filters.unsupervised.attribute.StringToWordVector;

public final class FeatureExtractor {

	static {
		Logging.configure();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureExtractor.class);

	public Dataset extractFeatures(final IndexReader indexReader) {
		for (int i = 0; i < indexReader.numDocs(); i++) {
			try {
				Document document = indexReader.document(i);
				List<Fieldable> fieldables = document.getFields();
				for (final Fieldable fieldable : fieldables) {
					TermFreqVector termFreqVector = indexReader.getTermFreqVector(i, fieldable.name());
					String[] terms = termFreqVector.getTerms();
					int[] termFrequencies = termFreqVector.getTermFrequencies();
					for (final String term : terms) {
						// DenseInstance denseInstance = new DenseInstance(att, classValue);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Test
	public void extractFeatures() {
		// What wonderful world, love all need
		double[] positiveVector = new double[] { 1, 1, 1, 1, 1, 1 };
		Instance positiveInstance = new DenseInstance(positiveVector, "positive");
		double[] negativeVector = new double[] { 0, 0, 0, 0, 0, 0 };
		Instance negativeInstance = new DenseInstance(negativeVector, "negative");
		Dataset dataset = new DefaultDataset(Arrays.asList(positiveInstance, negativeInstance));

		LibSVM libSvmClassifier = new LibSVM();
		libSvmClassifier.buildClassifier(dataset);

		// Hello world, love ya
		double[] toClassifyVector = new double[] { 0, 1, 1, 1, 1, 0 };
		Instance toClassifyInstance = new DenseInstance(toClassifyVector);
		Object result = libSvmClassifier.classify(toClassifyInstance);
		LOGGER.info("Result : " + result);
	}

	public void extractDictionary(final String text) {
	}

	private void stringToWordVector() {
		StringToWordVector stringToWordVector = new StringToWordVector();
	}

}
