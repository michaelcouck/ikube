package ikube.analytics;

//import java.io.IOException;
//import java.io.StringReader;
//import java.util.Arrays;
//import java.util.LinkedList;
//import java.util.List;
//
//import libsvm.LibSVM;
//import net.sf.javaml.core.Dataset;
//import net.sf.javaml.core.DefaultDataset;
//import net.sf.javaml.core.DenseInstance;
//import net.sf.javaml.core.Instance;
//
//import org.apache.lucene.analysis.Tokenizer;
//import org.apache.lucene.analysis.standard.StandardTokenizer;
//import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.document.Fieldable;
//import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.index.TermFreqVector;
//import org.apache.lucene.util.Version;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public final class FeatureExtractor {
//
//	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureExtractor.class);
//
//	public Dataset extractFeatures(final IndexReader indexReader) {
//		for (int i = 0; i < indexReader.numDocs(); i++) {
//			try {
//				Document document = indexReader.document(i);
//				List<Fieldable> fieldables = document.getFields();
//				for (final Fieldable fieldable : fieldables) {
//					TermFreqVector termFreqVector = indexReader.getTermFreqVector(i, fieldable.name());
//					String[] terms = termFreqVector.getTerms();
//					int[] termFrequencies = termFreqVector.getTermFrequencies();
//					for (final String term : terms) {
//						// DenseInstance denseInstance = new DenseInstance(att, classValue);
//					}
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return null;
//	}
//
//	@Test
//	public void extractFeatures() throws IOException {
//		double[] positiveVector = extractFeatures("this wonderful world love is all you need"); // new double[] { 1, 1, 1, 1 };
//		Instance positiveInstance = new DenseInstance(positiveVector, "positive");
//		double[] negativeVector = extractFeatures("shit what a lousy day"); // new double[] { 0, 0, 0, 0 };
//		Instance negativeInstance = new DenseInstance(negativeVector, "negative");
//		Dataset dataset = new DefaultDataset(Arrays.asList(positiveInstance, negativeInstance));
//
//		LibSVM libSvmClassifier = new LibSVM();
//		libSvmClassifier.buildClassifier(dataset);
//
//		double[] toClassifyVector = extractFeatures("love");
//		Instance toClassifyInstance = new DenseInstance(toClassifyVector);
//		Object result = libSvmClassifier.classify(toClassifyInstance);
//		LOGGER.info("Result : " + result);
//	}
//
//	int minGram = 3;
//	int maxGram = 21;
//
//	private double[] extractFeatures(final String text) throws IOException {
//		LinkedList<String> dictionary = extractDictionary();
//		double[] features = new double[dictionary.size()];
//		Tokenizer nGramTokenizer = null;
//		try {
//			nGramTokenizer = new StandardTokenizer(Version.LUCENE_36, new StringReader(text));
//			CharTermAttribute charTermAttribute = nGramTokenizer.addAttribute(CharTermAttribute.class);
//			while (nGramTokenizer.incrementToken()) {
//				String token = charTermAttribute.toString();
//				int index = dictionary.indexOf(token);
//				if (index > -1) {
//					features[index]++;
//				}
//			}
//		} finally {
//			if (nGramTokenizer != null) {
//				nGramTokenizer.close();
//			}
//		}
//		return features;
//	}
//
//	public LinkedList<String> extractDictionary() throws IOException {
//		LinkedList<String> dictionary = new LinkedList<String>();
//		String text = "this wonderful world love all you need shit what a lousy day";
//		Tokenizer nGramTokenizer = new StandardTokenizer(Version.LUCENE_CURRENT, new StringReader(text));
//		try {
//			CharTermAttribute charTermAttribute = nGramTokenizer.addAttribute(CharTermAttribute.class);
//			while (nGramTokenizer.incrementToken()) {
//				String token = charTermAttribute.toString();
//				if (!dictionary.contains(token)) {
//					dictionary.push(token);
//				}
//			}
//		} finally {
//			nGramTokenizer.close();
//		}
//		return dictionary;
//	}
//
//}