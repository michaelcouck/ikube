package ikube.action.index.analyzer;

import ikube.IConstants;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;

/**
 * This analyzer will break the data into n-grams. This means that there will potentially be several times the number of words and indeed in the data. This
 * facilitates fuzzy searching for sub-strings rather than wild cards which are very expensive, but will add considerably to the size of the index.
 * 
 * @author Michael Couck
 * @since 29.10.12
 * @version 01.00
 */
public final class NgramAnalyzer extends Analyzer {

	private int minGram = 4;
	private int maxGram = 21;

	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		Tokenizer tokenizer = new LowerCaseTokenizer(IConstants.LUCENE_VERSION, reader);
		NGramTokenFilter nGramTokenFilter = new NGramTokenFilter(IConstants.LUCENE_VERSION, tokenizer, minGram, maxGram);
		return new TokenStreamComponents(tokenizer, nGramTokenFilter);
	}

	public void setMinGram(int minGram) {
		this.minGram = minGram;
	}

	public void setMaxGram(int maxGram) {
		this.maxGram = maxGram;
	}

}
