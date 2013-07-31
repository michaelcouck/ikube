package ikube.analytics;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public final class FeatureExtractor {

	int minGram = 3;
	int maxGram = 21;
	private LinkedList<String> dictionary = new LinkedList<String>();

	public synchronized double[] extractFeatures(final String text, final String... dictionaryTerms) throws IOException {
		if (dictionaryTerms != null && dictionaryTerms.length > 0) {
			for (final String dictionaryTerm : dictionaryTerms) {
				addToDictionary(dictionaryTerm);
			}
		}
		double[] features = new double[dictionary.size()];
		Tokenizer tokenizer = getTokenizer(text);
		try {
			CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
			while (tokenizer.incrementToken()) {
				String token = charTermAttribute.toString();
				int index = dictionary.indexOf(token);
				if (index > -1) {
					features[index]++;
				}
			}
		} finally {
			tokenizer.close();
		}
		return features;
	}

	LinkedList<String> addToDictionary(final String text) throws IOException {
		Tokenizer tokenizer = getTokenizer(text);
		try {
			CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
			while (tokenizer.incrementToken()) {
				String token = charTermAttribute.toString();
				if (!dictionary.contains(token)) {
					dictionary.push(token);
				}
			}
		} finally {
			tokenizer.close();
		}
		return dictionary;
	}

	private Tokenizer getTokenizer(final String text) {
		// return new NGramTokenizer(new StringReader(text.toLowerCase()), minGram, maxGram);
		return new StandardTokenizer(Version.LUCENE_36, new StringReader(text.toLowerCase()));
	}

}