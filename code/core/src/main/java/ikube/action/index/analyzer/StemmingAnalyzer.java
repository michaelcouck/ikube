package ikube.action.index.analyzer;

import ikube.IConstants;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.springframework.beans.factory.annotation.Value;

import java.io.Reader;

/**
 * This analyzer will stem the words for plural and so on. All the stop words in all the languages that are available are also added to the analyzer, slowing
 * the performance down, but adding value to the results.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 29-10-2012
 */
public final class StemmingAnalyzer extends Analyzer {

    @Value("${stemming.analyzer.stop.words}")
    private boolean useStopWords = Boolean.TRUE;
    private CharArraySet stopWords;

    public void initialize() {
        if (useStopWords) {
            stopWords = getStopWords();
        }
    }

    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        LowerCaseTokenizer lowerCaseTokenizer = new LowerCaseTokenizer(IConstants.LUCENE_VERSION, reader);
        PorterStemFilter porterStemFilter = new PorterStemFilter(lowerCaseTokenizer);
        TokenFilter tokenFilter;
        if (stopWords == null || stopWords.size() == 0) {
            tokenFilter = porterStemFilter;
        } else {
            tokenFilter = new StopFilter(IConstants.LUCENE_VERSION, porterStemFilter, stopWords);
        }
        return new TokenStreamComponents(lowerCaseTokenizer, tokenFilter);
    }

    @SuppressWarnings({"unchecked"})
    private CharArraySet getStopWords() {
        CharArraySet stopWords = new CharArraySet(IConstants.LUCENE_VERSION, 10, Boolean.TRUE);
        stopWords.addAll(GreekAnalyzer.getDefaultStopSet());
        stopWords.addAll(CzechAnalyzer.getDefaultStopSet());
        stopWords.addAll(DutchAnalyzer.getDefaultStopSet());
        stopWords.addAll(FrenchAnalyzer.getDefaultStopSet());
        stopWords.addAll(GermanAnalyzer.getDefaultStopSet());
        stopWords.addAll(BrazilianAnalyzer.getDefaultStopSet());
        stopWords.addAll(RussianAnalyzer.getDefaultStopSet());
        stopWords.addAll(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
        return stopWords;
    }

    public void setUseStopWords(boolean useStopWords) {
        this.useStopWords = useStopWords;
    }

}