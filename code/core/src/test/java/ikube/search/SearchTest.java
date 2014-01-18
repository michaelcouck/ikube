package ikube.search;

import ikube.AbstractTest;
import ikube.action.index.analyzer.StemmingAnalyzer;
import ikube.search.Search.TypeField;
import org.apache.lucene.analysis.Analyzer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static ikube.IConstants.*;
import static org.junit.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 12.10.2010
 */
@SuppressWarnings("deprecation")
public class SearchTest extends AbstractTest {

	private String russian = "Россия   русский язык  ";
	private String german = "Produktivität";
	private String french = "productivité";
	private String english = "productivity";
	private String somthingElseAlToGether = "Soleymān Khāţer";
	private String string = "Qu'est ce qui détermine la productivité, et comment est-il mesuré? " //
		+ "Was bestimmt die Produktivität, und wie wird sie gemessen? " //
		+ "русский язык " + //
		"Soleymān Khāţer Solţānābād " + //
		russian + " " + //
		german + " " + //
		french + " " + //
		somthingElseAlToGether + " ";
	private String somethingNumeric = " 123.456789 ";
	private String[] strings = {
		russian,
		german,
		french,
		english,
		somthingElseAlToGether,
		string,
		somethingNumeric,
		"123.456789",
		"123.456790",
		"123.456791",
		"123.456792",
		"123.456793",
		"123.456794",
		"123456789",
		"123456790"
	};
	private Analyzer analyzer;

	@Before
	public void before() throws Exception {
		analyzer = new StemmingAnalyzer();
	}

	@Test
	public void searchSingle() throws Exception {
		SearchComplex searchSingle = createIndexRamAndSearch(SearchComplex.class, analyzer, ID, strings);
		searchSingle.setFirstResult(0);
		searchSingle.setMaxResults(10);
		searchSingle.setFragment(Boolean.TRUE);

		searchSingle.setSearchFields(ID);
		searchSingle.setSearchStrings(english);
		searchSingle.setOccurrenceFields(SHOULD);
		searchSingle.setTypeFields(STRING);

		ArrayList<HashMap<String, String>> results = searchSingle.execute();
		printResults(results);
		assertTrue(results.size() > 1);
	}

	@Test
	public void searchMulti() throws Exception {
		SearchComplex searchMulti = createIndexRamAndSearch(SearchComplex.class, analyzer, ID, strings);
		searchMulti.setFirstResult(0);
		searchMulti.setFragment(Boolean.TRUE);
		searchMulti.setMaxResults(10);
		searchMulti.setSearchFields(ID, ID);
		searchMulti.setSearchStrings("Soleymā~", russian);
		searchMulti.setTypeFields(STRING, STRING);
		searchMulti.setOccurrenceFields(MUST, MUST);
		ArrayList<HashMap<String, String>> results = searchMulti.execute();
		assertTrue(results.size() > 1);
	}

	@Test
	public void searchMultiSorted() throws Exception {
		SearchComplexSorted searchMultiSorted = createIndexRamAndSearch(SearchComplexSorted.class, analyzer, CONTENTS,
			"12345.0", "1234.0", "123.0");
		searchMultiSorted.setFirstResult(0);
		searchMultiSorted.setMaxResults(10);
		searchMultiSorted.setFragment(Boolean.TRUE);

		searchMultiSorted.setSearchFields(CONTENTS);
		searchMultiSorted.setSearchStrings("123.0-123456.0");
		searchMultiSorted.setTypeFields(TypeField.RANGE.name());
		searchMultiSorted.setOccurrenceFields(MUST);
		searchMultiSorted.setSortFields(ID);
		searchMultiSorted.setSortDirections(Boolean.TRUE.toString());

		ArrayList<HashMap<String, String>> results = searchMultiSorted.execute();
		printResults(results);
		assertTrue(results.size() > 1);
		// Remove the statistics
		results.remove(results.size() - 1);

		// TODO The sort doesn't seem to work at all!!!!
		// Verify that all the results are in ascending order according to the id
		String previousId = "0.0";
		for (Map<String, String> result : results) {
			String id = result.get(ID);
			assertTrue(Double.parseDouble(previousId) <= Double.parseDouble(id));
			previousId = id;
		}
	}

	@Test
	public void searchNumeric() throws Exception {
		SearchComplex searchNumericAll = createIndexRamAndSearch(SearchComplex.class, analyzer, CONTENTS, strings);
		searchNumericAll.setFirstResult(0);
		searchNumericAll.setFragment(Boolean.TRUE);
		searchNumericAll.setMaxResults(10);
		searchNumericAll.setSearchFields(CONTENTS);
		searchNumericAll.setSearchStrings("123456790");
		searchNumericAll.setOccurrenceFields(SHOULD);
		searchNumericAll.setTypeFields(TypeField.NUMERIC.name());

		ArrayList<HashMap<String, String>> results = searchNumericAll.execute();
		assertTrue(results.size() > 1);

		searchNumericAll.setSearchStrings("123.456790");
		results = searchNumericAll.execute();
		assertTrue(results.size() > 1);
	}

	@Test
	public void searchNumericRange() throws Exception {
		SearchComplex searchNumericRange = createIndexRamAndSearch(SearchComplex.class, analyzer, CONTENTS, strings);
		searchNumericRange.setFirstResult(0);
		searchNumericRange.setFragment(Boolean.TRUE);
		searchNumericRange.setMaxResults(10);
		searchNumericRange.setSearchFields(CONTENTS);
		searchNumericRange.setSearchStrings("123.456790-123.456796");
		searchNumericRange.setTypeFields(TypeField.RANGE.fieldType());
		searchNumericRange.setOccurrenceFields(SHOULD);

		ArrayList<HashMap<String, String>> results = searchNumericRange.execute();
		assertTrue(results.size() > 1);

		searchNumericRange.setSearchStrings("888888888-999999999");
		results = searchNumericRange.execute();
		assertEquals("There should be no results, i.e. only the statistics from this range : ", 1, results.size());

		searchNumericRange.setSearchStrings("111-122");
		results = searchNumericRange.execute();
		assertEquals("There should be no results, i.e. only the statistics from this range : ", 1, results.size());
	}

	@Test
	public void addStatistics() throws Exception {
		String searchString = "michael AND couck";
		SearchComplex search = createIndexRamAndSearch(SearchComplex.class, analyzer, CONTENTS, strings);
		search.setSearchStrings(searchString);

		ArrayList<HashMap<String, String>> results = new ArrayList<>();
		search.addStatistics(new String[]{searchString}, results, 79, 0.0f, 23, null);
		Map<String, String> statistics = results.get(results.size() - 1);
		logger.info("Search strings : " + statistics.get(SEARCH_STRINGS));
		logger.info("Corrected search strings : " + statistics.get(CORRECTIONS));
		assertEquals("michael AND couck", statistics.get(SEARCH_STRINGS));
		assertNotNull("Should be something like : michael and couch : ", statistics.get(CORRECTIONS));
	}

	@Test
	public void getCorrections() throws Exception {
		String[] searchStrings = {"some correct words", "unt soome are niet corekt",
			"AND there are AND some gobblie words WITH AND another"};
		SearchComplex search = createIndexRamAndSearch(SearchComplex.class, analyzer, CONTENTS, strings);
		search.setSearchStrings(searchStrings);
		String[] correctedSearchStrings = search.getCorrections(searchStrings);
		for (int i = 0; i < searchStrings.length; i++) {
			assertEquals("Search strings must be the same : ", searchStrings[i], correctedSearchStrings[i]);
		}
	}

}