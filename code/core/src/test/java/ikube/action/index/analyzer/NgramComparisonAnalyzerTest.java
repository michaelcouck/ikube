package ikube.action.index.analyzer;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.search.Search;
import ikube.search.SearchComplex;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This test is to see visually what the differences are in teh analyzers' results.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 16.01.14
 */
public class NgramComparisonAnalyzerTest extends AbstractTest {

    @Test
    public void nGram() throws Exception {
        NgramAnalyzer ngramAnalyzer = new NgramAnalyzer();
        ngramAnalyzer.setMinGram(3);

        String[] strings = {"hello", "world", "aello", "aella", "competent", "incompetent"};
        SearchComplex search = createIndexRamAndSearch(SearchComplex.class, ngramAnalyzer, IConstants.CONTENT, strings);
        search.setFirstResult(0);
        search.setFragment(true);
        search.setMaxResults(10);
        search.setSearchFields(IConstants.CONTENT);
        search.setOccurrenceFields(IConstants.MUST);
        search.setTypeFields(IConstants.STRING);

        doSearch("hello world", "<B>world</B>", search);
        doSearch("hello", "<B>hello</B>", search);
        doSearch("world", "<B>world</B>", search);

        doSearch("comp", "<B>competen</B>t", search);
        doSearch("incomp", "<B>incompet</B>ent", search);
    }

    @Test
    public void edgeNGram() throws Exception {
        EdgeNgramAnalyzer edgeNgramAnalyzer;
        edgeNgramAnalyzer = new EdgeNgramAnalyzer();
        edgeNgramAnalyzer.setMinGram(3);

        String[] strings = {"hello", "world", "aello", "aella", "competent", "incompetent"};
        SearchComplex search = createIndexRamAndSearch(SearchComplex.class, edgeNgramAnalyzer, IConstants.CONTENT, strings);
        search.setFirstResult(0);
        search.setFragment(true);
        search.setMaxResults(10);
        search.setSearchFields(IConstants.CONTENT);
        search.setOccurrenceFields(IConstants.MUST);
        search.setTypeFields(IConstants.STRING);

        doSearch("hello world", "<B>world</B>", search);
        doSearch("hello", "<B>hello</B>", search);
        doSearch("world", "<B>world</B>", search);

        doSearch("comp", "<B>competen</B>t", search);
        doSearch("incomp", "<B>incompet</B>ent", search);
    }

    private void doSearch(final String searchString, final String expected, final Search search) {
        search.setSearchStrings(searchString);
        ArrayList<HashMap<String, String>> results = search.execute();
        Map<String, String> result = results.get(0);
        String fragment = result.get(IConstants.FRAGMENT);
        logger.info("Search string : " + searchString + ", expected : " + expected + "-" + fragment);
    }

}