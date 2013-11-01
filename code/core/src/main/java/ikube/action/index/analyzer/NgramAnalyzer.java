package ikube.action.index.analyzer;

import ikube.IConstants;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.shingle.ShingleMatrixFilter;

/**
 * This analyzer will break the data into n-grams. This means that there will potentially be several times the number of words and indeed in
 * the data. This facilitates fuzzy searching for sub-strings rather than wild cards which are very expensive, but will add considerably to
 * the size of the index.
 * 
 * @author Michael Couck
 * @since 29.10.12
 * @version 01.00
 */
public final class NgramAnalyzer extends Analyzer {

	private int minGram = 3;
	private int maxGram = 12;

	/**
	 * This method will produce n-grams from the text stream. We don't want to stop the words and we don't need to stem them because n-grams
	 * are essentially the stems.
	 * 
	 * Possible alternatives are the {@link NGramTokenizer}. We can't use the {@link ShingleAnalyzerWrapper} and the
	 * {@link ShingleMatrixFilter} with a lower case filter because this splits the text bi-word strings, not n-gram strings.
	 */
	@Override
	public final TokenStream tokenStream(final String fieldName, final Reader reader) {
		Tokenizer tokenizer = new LowerCaseTokenizer(IConstants.VERSION, reader);
		return new NGramTokenFilter(tokenizer, minGram, maxGram);
	}

	public void setMinGram(int minGram) {
		this.minGram = minGram;
	}

	public void setMaxGram(int maxGram) {
		this.maxGram = maxGram;
	}

}
