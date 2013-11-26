package ikube.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.analyzer.NgramAnalyzer;
import ikube.search.Search.TypeField;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 07.10.2013
 * @version 01.00
 */
public class SearchComplexSortedTest extends AbstractTest {

	private SearchComplexSorted searchComplexSorted;

	@Before
	public void before() throws Exception {
		NgramAnalyzer analyzer = new NgramAnalyzer();
		analyzer.setMinGram(3);
		searchComplexSorted = createIndexRamAndSearch(SearchComplexSorted.class, analyzer, IConstants.CONTENT, "123456", "234567", "345678", "456789", "abc123");
	}

	@Test
	public void getQueryAndSearch() throws ParseException {
		searchComplexSorted.setFirstResult(0);
		searchComplexSorted.setFragment(Boolean.TRUE);
		searchComplexSorted.setMaxResults(10);
		searchComplexSorted.setSearchField(IConstants.CONTENT);
		searchComplexSorted.setSearchString("123456");
		searchComplexSorted.setSortField(IConstants.CONTENT);
		searchComplexSorted.setTypeFields(TypeField.NUMERIC.fieldType());
		Query query = searchComplexSorted.getQuery();
		assertNotNull(query);

		ArrayList<HashMap<String, String>> results = searchComplexSorted.execute();
		assertEquals("Should be the statistics and a result : ", 2, results.size());
	}

	@Test
	public void searchAlphanumeric() throws ParseException {
		searchComplexSorted.setFirstResult(0);
		searchComplexSorted.setFragment(Boolean.TRUE);
		searchComplexSorted.setMaxResults(10);
		searchComplexSorted.setSearchField(IConstants.CONTENT);
		searchComplexSorted.setSearchString("abc123");
		searchComplexSorted.setSortField(IConstants.CONTENT);
		searchComplexSorted.setTypeFields(TypeField.STRING.fieldType());

		Query query = searchComplexSorted.getQuery();
		assertNotNull(query);

		ArrayList<HashMap<String, String>> results = searchComplexSorted.execute();
		assertEquals("Should be the statistics and a result : ", 2, results.size());

		class New implements AutoCloseable {
			@Override
			public void close() throws Exception {
			}
		}

		try (New o = new New()) {
			o.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}