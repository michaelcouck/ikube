package ikube.action.index.analyzer;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.search.SearchComplex;
import org.apache.lucene.analysis.TokenStream;
import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29.10.12
 */
public class StemmingAnalyzerTest extends AbstractTest {

    private StemmingAnalyzer stemmingAnalyzer;

    @Before
    public void before() {
        stemmingAnalyzer = new StemmingAnalyzer();
        stemmingAnalyzer.setUseStopWords(Boolean.TRUE);
        stemmingAnalyzer.initialize();
    }

    @Test
    public void tokenStream() throws Exception {
        Reader reader = new StringReader("The string to break into tokens");
        TokenStream tokenStream = stemmingAnalyzer.tokenStream(IConstants.CONTENT, reader);
        assertNotNull(tokenStream);
    }

    @Test
    public void endToEnd() throws Exception {
        SearchComplex searchSingle = createIndexRamAndSearch(SearchComplex.class, stemmingAnalyzer,
            IConstants.CONTENT, "duck", "ducks", "peoples", "peopled", "oranges", "orange", "the large cake");
        searchSingle.setFirstResult(0);
        searchSingle.setFragment(true);
        searchSingle.setMaxResults(10);
        searchSingle.setSearchFields(IConstants.CONTENT);
        searchSingle.setSearchStrings("duck peoples orange");
        searchSingle.setSortFields(IConstants.CONTENT);

        ArrayList<HashMap<String, String>> results = searchSingle.execute();

        assertEquals("All the documents should be hit : ", 7, results.size());
        assertEquals("This is the highlighted hit : ", "<B>ducks</B>", results.get(1).get(IConstants.FRAGMENT));

        // Try a stop word
        searchSingle.setSearchStrings("the");
        results = searchSingle.execute();
        assertEquals("There should be no hits : ", 1, results.size());

        // Try the other words in the document
        searchSingle.setSearchStrings("the large cake");
        results = searchSingle.execute();
        assertEquals("There should be one hit : ", 2, results.size());
    }

}
