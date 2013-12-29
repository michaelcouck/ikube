package ikube.search;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.analyzer.NgramAnalyzer;
import ikube.action.index.analyzer.StemmingAnalyzer;
import ikube.mock.SpellingCheckerMock;
import ikube.search.Search.TypeField;
import ikube.search.spelling.SpellingChecker;
import ikube.toolkit.FileUtilities;
import mockit.Deencapsulation;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

	@Before
	public void before() throws Exception {
		SpellingChecker checkerExt = new SpellingChecker();
		Deencapsulation.setField(checkerExt, "languageWordListsDirectory", "languages");
		Deencapsulation.setField(checkerExt, "spellingIndexDirectoryPath", "./spellingIndex");
		checkerExt.initialize();
	}

	@After
	public void after() throws Exception {
		Mockit.tearDownMocks();
	}

	@Test
	public void searchSingle() throws Exception {
		SearchComplex searchSingle = createIndexRamAndSearch(SearchComplex.class, new StemmingAnalyzer(), IConstants.CONTENTS, strings);
		searchSingle.setFirstResult(0);
		searchSingle.setFragment(Boolean.TRUE);
		searchSingle.setMaxResults(10);
		searchSingle.setSearchFields(IConstants.CONTENTS);
		searchSingle.setSearchStrings(russian);
		searchSingle.setSortField(new String[]{IConstants.ID});
		ArrayList<HashMap<String, String>> results = searchSingle.execute();
		assertTrue(results.size() > 1);
	}

	@Test
	public void searchMulti() throws Exception {
		SearchComplex searchMulti = createIndexRamAndSearch(SearchComplex.class, new NgramAnalyzer(), IConstants.ID, strings);
		searchMulti.setFirstResult(0);
		searchMulti.setFragment(Boolean.TRUE);
		searchMulti.setMaxResults(10);
		searchMulti.setSearchFields(IConstants.ID);
		searchMulti.setSearchStrings("id.123~"); // , "id.1~"
		searchMulti.setTypeFields(IConstants.STRING);
		searchMulti.setOccurrenceFields(IConstants.SHOULD);
		ArrayList<HashMap<String, String>> results = searchMulti.execute();
		assertTrue(results.size() > 1);
	}

	@Test
	public void searchMultiSorted() throws Exception {
		SearchComplexSorted searchMultiSorted = createIndexRamAndSearch(SearchComplexSorted.class, new StemmingAnalyzer(), IConstants.ID, strings);
		searchMultiSorted.setFirstResult(0);
		searchMultiSorted.setFragment(Boolean.TRUE);
		searchMultiSorted.setMaxResults(10);
		searchMultiSorted.setSearchFields(IConstants.ID, IConstants.CONTENTS);
		searchMultiSorted.setSearchStrings("id.1~", "hello");
		searchMultiSorted.setSortField(new String[]{IConstants.ID});
		ArrayList<HashMap<String, String>> results = searchMultiSorted.execute();
		assertTrue(results.size() > 1);

		// Verify that all the results are in ascending order according to the id
		String previousId = null;
		for (Map<String, String> result : results) {
			String id = result.get(IConstants.ID);
			logger.info("Previous id : " + previousId + ", id : " + id);
			if (previousId != null && id != null) {
				assertTrue(previousId.compareTo(id) <= 0);
			}
			previousId = id;
		}
	}

	@Test
	@Deprecated
	public void searchNumeric() throws Exception {
		SearchComplex searchNumericAll = null; // (SearchComplex) getSearch(SearchComplex.class);
		searchNumericAll.setFirstResult(0);
		searchNumericAll.setFragment(Boolean.TRUE);
		searchNumericAll.setMaxResults(10);
		searchNumericAll.setSearchFields(IConstants.CONTENTS);
		searchNumericAll.setSearchStrings("123456790");
		ArrayList<HashMap<String, String>> results = searchNumericAll.execute();
		assertTrue(results.size() > 1);

		searchNumericAll.setSearchStrings("123.456790");
		results = searchNumericAll.execute();
		assertTrue(results.size() > 1);
	}

	@Test
	public void searchNumericRange() throws Exception {
		SearchComplex searchNumericRange = null; // (SearchComplex) getSearch(SearchComplex.class);
		searchNumericRange.setFirstResult(0);
		searchNumericRange.setFragment(Boolean.TRUE);
		searchNumericRange.setMaxResults(10);
		searchNumericRange.setSearchFields(IConstants.CONTENTS);
		searchNumericRange.setSearchStrings("123.456790-123.456796");
		searchNumericRange.setTypeFields(TypeField.RANGE.fieldType());
		ArrayList<HashMap<String, String>> results = searchNumericRange.execute();
		assertTrue(results.size() > 1);

		searchNumericRange.setSearchStrings("888888888-999999999");
		results = searchNumericRange.execute();
		assertEquals("There shoud be no results, i.e. only the statistics from this range : ", 1, results.size());

		searchNumericRange.setSearchStrings("111-122");
		results = searchNumericRange.execute();
		assertEquals("There shoud be no results, i.e. only the statistics from this range : ", 1, results.size());
	}

	@Test
	public void addStatistics() throws Exception {
		String searchString = "michael AND couck";
		Search search = null; // getSearch(SearchComplex.class);
		search.setSearchStrings(searchString);

		ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
		search.addStatistics(new String[]{searchString}, results, 79, 0.0f, 23, null);
		Map<String, String> statistics = results.get(results.size() - 1);
		logger.info("Search strings : " + statistics.get(IConstants.SEARCH_STRINGS));
		logger.info("Corrected search strings : " + statistics.get(IConstants.CORRECTIONS));
		assertEquals("michael AND couck", statistics.get(IConstants.SEARCH_STRINGS));
		assertNotNull("Should be something like : michael and couch : ", statistics.get(IConstants.CORRECTIONS));
	}

	@Test
	public void getCorrections() throws Exception {
		try {
			Mockit.tearDownMocks(SpellingChecker.class);
			SpellingChecker spellingChecker = new SpellingChecker();
			File languagesWordFileDirectory = FileUtilities.findFileRecursively(new File("."), "english.txt").getParentFile();
			Deencapsulation.setField(spellingChecker, "languageWordListsDirectory", languagesWordFileDirectory.getAbsolutePath());
			Deencapsulation.setField(spellingChecker, "spellingIndexDirectoryPath", "./spellingIndex");
			spellingChecker.initialize();

			String[] searchStrings = {"some correct words", "unt soome are niet corekt", "AND there are AND some gobblie words WITH AND another"};
			Search search = null; // getSearch(SearchComplex.class);
			search.setSearchStrings(searchStrings);
			String[] correctedSearchStrings = search.getCorrections(searchStrings);
			String correctedSearchString = Arrays.deepToString(correctedSearchStrings);
			logger.info("Corrected : " + correctedSearchString);
			assertEquals("Only the completely incorrect words should be replaced : ",
				"[some correct words, unct sooke are net coreen, AND there are AND some gobble words WITH AND another]", correctedSearchString);
		} finally {
			Mockit.setUpMock(SpellingCheckerMock.class);
		}
	}

}