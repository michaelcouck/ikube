package ikube.action.index.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.AbstractTest;
import ikube.IConstants;
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
public class StemmingAnalyzerTest extends AbstractTest {

	private StemmingAnalyzer stemmingAnalyzer;

	@Before
	public void before() {
		stemmingAnalyzer = new StemmingAnalyzer();
	}

	@Test
	public void tokenStream() throws Exception {
		Reader reader = new StringReader("The string to break into tokens");
		TokenStream tokenStream = stemmingAnalyzer.tokenStream(IConstants.CONTENT, reader);
		logger.info("Token stream : " + tokenStream + ", " + tokenStream.getClass());
		assertNotNull(tokenStream);
	}

	@Test
	public void endToEnd() throws Exception {
		SearchComplex searchSingle = createIndexRamAndSearch(SearchComplex.class, stemmingAnalyzer, IConstants.CONTENT, "duck", "ducks", "peoples", "peopled",
				"oranges", "orange");
		searchSingle.setFirstResult(0);
		searchSingle.setFragment(true);
		searchSingle.setMaxResults(10);
		searchSingle.setSearchField(IConstants.CONTENT);
		searchSingle.setSearchString("duck peoples orange");
		searchSingle.setSortField(IConstants.CONTENT);

		ArrayList<HashMap<String, String>> results = searchSingle.execute();
		logger.info("Results : " + results);

		assertEquals("All the documents should be hit : ", 7, results.size());
		assertEquals("This is the highlighted hit : ", "<B>ducks</B> ", results.get(1).get(IConstants.FRAGMENT));
	}

}
