package ikube.action.index.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.shingle.ShingleMatrixFilter;

/**
 * This analyzer will boost the edges of words. So for example dictionaries that have only one word, words that start with a certain character pattern will
 * score higher than those words that have the pattern in the middle of the word.
 * 
 * @author Michael Couck
 * @since 09.11.13
 * @version 01.00
 */
public final class EdgeNgramAnalyzer extends Analyzer {

	private int minGram = 3;
	private int maxGram = 32;

	/**
	 * This method will produce n-grams from the text stream.
	 * 
	 * Possible alternatives are the {@link NGramTokenizer}. We can't use the {@link ShingleAnalyzerWrapper} and the {@link ShingleMatrixFilter} with a lower
	 * case filter because this splits the text bi-word strings, not n-gram strings.
	 */
	@Override
	public final TokenStream tokenStream(final String fieldName, final Reader reader) {
		EdgeNGramTokenizer.Side side = EdgeNGramTokenizer.Side.FRONT;
		Tokenizer tokenizer = new EdgeNGramTokenizer(reader, side, minGram, maxGram);
		return new NGramTokenFilter(tokenizer, minGram, maxGram);
	}

	public void setMinGram(int minGram) {
		this.minGram = minGram;
	}

	public void setMaxGram(int maxGram) {
		this.maxGram = maxGram;
	}

}
