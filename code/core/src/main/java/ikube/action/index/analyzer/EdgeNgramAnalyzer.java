package ikube.action.index.analyzer;

import ikube.IConstants;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * This analyzer will boost the edges of words. So for example dictionaries that have only one word, words that start
 * with a certain character pattern will score higher than those words that have the pattern in the middle of the word.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-11-2013
 */
public final class EdgeNgramAnalyzer extends Analyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeNgramAnalyzer.class);

    private int minGram = 4;
    private int maxGram = 21;

    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        // EdgeNGramTokenizer side = EdgeNGramTokenizer.FRONT;
        // There is a problem in the edge n-gram filter so we have to create the reader again
        char[] cbuf = new char[1024];
        StringBuilder stringBuilder = new StringBuilder();
        try {
            int read;
            do {
                read = reader.read(cbuf);
                if (read < 0) {
                    break;
                }
                stringBuilder.append(cbuf, 0, read);
            } while (true);
        } catch (IOException e) {
            LOGGER.error(null, e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        Tokenizer tokenizer;
        StringReader stringReader = new StringReader(stringBuilder.toString());
        if (stringBuilder.length() == 0) {
            tokenizer = new LowerCaseTokenizer(IConstants.LUCENE_VERSION, stringReader);
        } else {
            tokenizer = new EdgeNGramTokenizer(IConstants.LUCENE_VERSION, stringReader, minGram, maxGram);
        }
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
