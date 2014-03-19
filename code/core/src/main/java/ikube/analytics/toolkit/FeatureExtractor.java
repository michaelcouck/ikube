package ikube.analytics.toolkit;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */
@Deprecated
public final class FeatureExtractor extends Filter implements UnsupervisedFilter {

    private LinkedList<String> dictionary = new LinkedList<>();

    public synchronized double[] extractFeatures(final String text, final String... dictionaryTerms) throws IOException {
        if (dictionaryTerms != null && dictionaryTerms.length > 0) {
            for (final String dictionaryTerm : dictionaryTerms) {
                addToDictionary(dictionaryTerm);
            }
        }
        double[] features = new double[dictionary.size()];
        try (Tokenizer tokenizer = getTokenizer(text)) {
            CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
            while (tokenizer.incrementToken()) {
                String token = charTermAttribute.toString();
                int index = dictionary.indexOf(token);
                if (index > -1) {
                    features[index]++;
                }
            }
        }
        return features;
    }

    LinkedList<String> addToDictionary(final String text) throws IOException {
        try (Tokenizer tokenizer = getTokenizer(text)) {
            CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
            while (tokenizer.incrementToken()) {
                String token = charTermAttribute.toString();
                if (!dictionary.contains(token)) {
                    dictionary.push(token);
                }
            }
        }
        return dictionary;
    }

    @SuppressWarnings("deprecation")
    private Tokenizer getTokenizer(final String text) {
        return new StandardTokenizer(Version.LUCENE_36, new StringReader(text.toLowerCase()));
    }

}