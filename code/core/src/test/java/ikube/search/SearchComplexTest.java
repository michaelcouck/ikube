package ikube.search;

import static ikube.search.Search.TypeField.NUMERIC;
import static ikube.search.Search.TypeField.RANGE;
import static ikube.search.Search.TypeField.STRING;
import static org.junit.Assert.assertEquals;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.analyzer.StemmingAnalyzer;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * NOTE to self: Lucene does not like underscores in the field names!!!
 * 
 * @author Michael Couck
 * @since 20.02.2012
 * @version 01.00
 */
public class SearchComplexTest extends AbstractTest {

	private SearchComplex searchComplex;

	@Before
	public void before() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), "index-data.csv");
		List<String> lines = FileUtils.readLines(file);
		String[] columns = StringUtils.split(lines.get(0), ';');
		List<String[]> data = new ArrayList<String[]>();

		for (final String line : lines) {
			String[] values = StringUtils.split(line, ';');
			data.add(values);
		}

		String[][] strings = data.toArray(new String[data.size()][]);
		strings = Arrays.copyOfRange(strings, 1, strings.length);
		searchComplex = createIndexRamAndSearch(SearchComplex.class, new StemmingAnalyzer(), columns, strings);
		searchComplex.setFirstResult(0);
		searchComplex.setFragment(true);
		searchComplex.setMaxResults(10);
	}

	@After
	@SuppressWarnings("deprecation")
	public void after() throws Exception {
		searchComplex.searcher.close();
	}

	@Test
	public void singleField() throws Exception {
		searchComplex.setSearchField("name");
		searchComplex.setSearchString("Michael Couck");
		searchComplex.setSortField("name");

		ArrayList<HashMap<String, String>> results = searchComplex.execute();
		String fragment = results.get(0).get(IConstants.FRAGMENT);
		assertEquals("This is the highlighted hit : ", "cv   <B>Michael</B> <B>Couck</B> ", fragment);
	}

	@Test
	public void numericQuery() throws Exception {
		searchComplex.setSearchField("annee");
		searchComplex.setSearchString("2001");
		searchComplex.setTypeFields(NUMERIC.fieldType());
		searchComplex.setSortField("annee");

		ArrayList<HashMap<String, String>> results = searchComplex.execute();
		assertEquals("There must be 1 result and the statistics : ", 2, results.size());

		searchComplex.setSearchString("2001", "6");
		searchComplex.setSearchField("annee", "column-two");
		searchComplex.setTypeFields(NUMERIC.fieldType(), NUMERIC.fieldType());
		searchComplex.setSortField("annee");

		results = searchComplex.execute();
		assertEquals("There must be 2 result and the statistics : ", 3, results.size());
	}

	@Test
	public void rangeQuery() throws Exception {
		searchComplex.setSearchField("annee");
		searchComplex.setSearchString("2001-2003");
		searchComplex.setTypeFields(RANGE.fieldType());
		searchComplex.setSortField("annee");
		ArrayList<HashMap<String, String>> results = searchComplex.execute();
		assertEquals("There must be 3 results and the statistics : ", 4, results.size());

		searchComplex.setSearchField("annee", "annee");
		searchComplex.setSearchString("2001-2003", "2005-2006");
		searchComplex.setTypeFields(RANGE.fieldType(), RANGE.fieldType());
		searchComplex.setSortField("annee");
		results = searchComplex.execute();
		assertEquals("There must be 5 results and the statistics : ", 6, results.size());

		searchComplex.setSearchField("column-two");
		searchComplex.setSearchString("5-7");
		searchComplex.setTypeFields(RANGE.fieldType());
		searchComplex.setSortField("column-two");
		results = searchComplex.execute();
		assertEquals("There must be 3 results and the statistics : ", 4, results.size());
	}

	@Test
	public void complexQuery() throws Exception {
		searchComplex.setSearchField("name", "annee", "column-two");
		searchComplex.setSearchString("cie interc", "2002", "7-9");
		searchComplex.setTypeFields(STRING.fieldType(), NUMERIC.fieldType(), RANGE.fieldType());
		searchComplex.setSortField("column-two");

		ArrayList<HashMap<String, String>> results = searchComplex.execute();
		assertEquals("There must be 6 results and the statistics : ", 7, results.size());
	}

}
