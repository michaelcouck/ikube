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

import org.apache.lucene.analysis.TokenStream;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 29.10.12
 * @version 01.00
 */
public class NgramAnalyzerTest extends AbstractTest {

	private NgramAnalyzer ngramAnalyzer;

	@Before
	public void before() {
		ngramAnalyzer = new NgramAnalyzer();
		ngramAnalyzer.setMinGram(3);
		ngramAnalyzer.setMaxGram(21);
	}

	@Test
	public void tokenStream() throws Exception {
		Reader reader = new StringReader("The string to break into tokens");
		TokenStream tokenStream = ngramAnalyzer.tokenStream(IConstants.CONTENT, reader);
		assertNotNull("Of course the stream can't be null : ", tokenStream);
	}

	@Test
	public void endToEnd() throws Exception {
		SearchComplex searchSingle = createIndexRamAndSearch(SearchComplex.class, ngramAnalyzer, IConstants.CONTENT, "Michael Couck");
		searchSingle.setFirstResult(0);
		searchSingle.setFragment(true);
		searchSingle.setMaxResults(10);
		searchSingle.setSearchFields(IConstants.CONTENT);
		searchSingle.setSearchStrings("hae ouc");
		searchSingle.setOccurrenceFields(IConstants.SHOULD);
		searchSingle.setTypeFields(Search.TypeField.STRING.name());
		// searchSingle.setSortFields(IConstants.CONTENT);

		ArrayList<HashMap<String, String>> results = searchSingle.execute();
		printResults(results);
		assertEquals("This is the highlighted hit : ", "<B>Michael</B> <B>Couck</B>", results.get(0).get(IConstants.FRAGMENT));

		searchSingle.setSearchStrings("poo");
		results = searchSingle.execute();
		assertEquals("There should be no results from the search : ", 1, results.size());
	}

}