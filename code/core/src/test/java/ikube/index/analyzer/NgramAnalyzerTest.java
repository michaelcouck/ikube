package ikube.index.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.ATest;
import ikube.IConstants;
import ikube.search.SearchSingle;

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
public class NgramAnalyzerTest extends ATest {

	private NgramAnalyzer ngramAnalyzer;

	public NgramAnalyzerTest() {
		super(NgramAnalyzerTest.class);
	}

	@Before
	public void before() {
		ngramAnalyzer = new NgramAnalyzer();
	}

	@Test
	public void tokenStream() throws Exception {
		Reader reader = new StringReader("The string to break into tokens");
		TokenStream tokenStream = ngramAnalyzer.tokenStream(IConstants.CONTENT, reader);
		assertNotNull("Of course the stream can't be null : ", tokenStream);
	}

	@Test
	public void endToEnd() throws Exception {
		SearchSingle searchSingle = createIndexAndSearch(SearchSingle.class, ngramAnalyzer, IConstants.CONTENT, "Michael Couck");
		searchSingle.setFirstResult(0);
		searchSingle.setFragment(true);
		searchSingle.setMaxResults(10);
		searchSingle.setSearchField(IConstants.CONTENT);
		searchSingle.setSearchString("mi ouc");
		searchSingle.setSortField(IConstants.CONTENT);

		ArrayList<HashMap<String, String>> results = searchSingle.execute();
		logger.info("Results : " + results);

		assertEquals("This is the highlighted hit : ", "<B>Mi</B>chael C<B>ouc</B>k ", results.get(0).get(IConstants.FRAGMENT));
	}

}