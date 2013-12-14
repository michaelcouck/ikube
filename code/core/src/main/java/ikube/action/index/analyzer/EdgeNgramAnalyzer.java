package ikube.action.index.analyzer;

import ikube.IConstants;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.shingle.ShingleMatrixFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This analyzer will boost the edges of words. So for example dictionaries that have only one word, words that start with a certain character pattern will
 * score higher than those words that have the pattern in the middle of the word.
 * 
 * @author Michael Couck
 * @since 09.11.13
 * @version 01.00
 */
public final class EdgeNgramAnalyzer extends Analyzer {

	private static final Logger LOGGER = LoggerFactory.getLogger(EdgeNgramAnalyzer.class);

	private int minGram = 4;
	private int maxGram = 21;

	/**
	 * This method will produce n-grams from the text stream.
	 * 
	 * Possible alternatives are the {@link NGramTokenizer}. We can't use the {@link ShingleAnalyzerWrapper} and the {@link ShingleMatrixFilter} with a lower
	 * case filter because this splits the text bi-word strings, not n-gram strings.
	 */
	@Override
	public final TokenStream tokenStream(final String fieldName, final Reader reader) {
		EdgeNGramTokenizer.Side side = EdgeNGramTokenizer.Side.FRONT;
		// There is a problem in the edge n-gram filter so we have to create the reader again
		int read = -1;
		char[] cbuf = new char[1024];
		StringBuilder stringBuilder = new StringBuilder();
		try {
			while ((read = reader.read(cbuf)) > 0) {
				stringBuilder.append(cbuf, 0, read);
			}
		} catch (IOException e) {
			LOGGER.error(null, e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
		StringReader stringReader = new StringReader(stringBuilder.toString());
		if (stringBuilder.length() == 0) {
			return new LowerCaseTokenizer(IConstants.VERSION, stringReader);
		}
		return new EdgeNGramTokenizer(stringReader, side, minGram, maxGram);
		// return new NGramTokenFilter(tokenizer, minGram, maxGram);
	}

	public void setMinGram(int minGram) {
		this.minGram = minGram;
	}

	public void setMaxGram(int maxGram) {
		this.maxGram = maxGram;
	}

}
