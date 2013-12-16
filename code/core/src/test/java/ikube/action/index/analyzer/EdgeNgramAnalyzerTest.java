package ikube.action.index.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.search.Search;
import ikube.search.SearchComplex;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 09.11.13
 * @version 01.00
 */
public class EdgeNgramAnalyzerTest extends AbstractTest {

	private EdgeNgramAnalyzer edgeNgramAnalyzer;

	@Before
	public void before() {
		edgeNgramAnalyzer = new EdgeNgramAnalyzer();
		edgeNgramAnalyzer.setMinGram(3);
	}

	@Test
	public void tokenStream() throws Exception {
		Reader reader = new StringReader("The string to break into tokens");
		TokenStream tokenStream = edgeNgramAnalyzer.tokenStream(IConstants.CONTENT, reader);
		assertNotNull("Of course the stream can't be null : ", tokenStream);
	}

	@Test
	public void endToEnd() throws Exception {
		String[] strings = { "hello", "world", "aello", "aella", "competent", "incompetent" };
		SearchComplex search = createIndexRamAndSearch(SearchComplex.class, edgeNgramAnalyzer, IConstants.CONTENT, strings);
		search.setFirstResult(0);
		search.setFragment(true);
		search.setMaxResults(10);
		search.setSearchField(IConstants.CONTENT);
		search.setSortField(IConstants.CONTENT);

		doSearch("hello world", "<B>hello</B> ", search);
		doSearch("hello", "<B>hello</B> ", search);
		doSearch("world", "<B>world</B> ", search);

		doSearch("comp", "<B>comp</B>etent ", search);
		doSearch("incom", "<B>incom</B>petent ", search);
	}

	private void doSearch(final String searchString, final String expected, final Search search) {
		search.setSearchString(searchString);
		ArrayList<HashMap<String, String>> results = search.execute();
		Map<String, String> result = results.get(0);
		String fragment = result.get(IConstants.FRAGMENT);
		assertEquals(expected, fragment);
	}

}