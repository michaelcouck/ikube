package ikube.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.mock.ReaderUtilMock;
import ikube.mock.SpellingCheckerMock;
import ikube.search.spelling.SpellingChecker;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.StringUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.ReaderUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchTest extends ATest {

	private static Searcher SEARCHER;
	private static String INDEX_DIRECTORY_PATH = "./" + SearchTest.class.getSimpleName();

	private static String russian = "Россия   русский язык  ";
	private static String german = "Produktivität";
	private static String french = "productivité";
	private static String somthingElseAlToGether = "Soleymān Khāţer";
	private static String string = "Qu'est ce qui détermine la productivité, et comment est-il mesuré? " //
			+ "Was bestimmt die Produktivität, und wie wird sie gemessen? " //
			+ "русский язык " + //
			"Soleymān Khāţer Solţānābād " + //
			russian + " " + //
			german + " " + //
			french + " " + //
			somthingElseAlToGether + " ";
	private static String somethingNumeric = " 123.456789 ";

	private static String[] strings = { russian, german, french, somthingElseAlToGether, string, somethingNumeric, "123.456789",
			"123.456790", "123.456791", "123.456792", "123.456793", "123.456794", "123456789", "123456790" };

	@Before
	public void before() {
		Mockit.setUpMock(SpellingCheckerMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(SpellingChecker.class);
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		Mockit.setUpMocks(ReaderUtilMock.class);
		SpellingChecker checkerExt = new SpellingChecker();
		Deencapsulation.setField(checkerExt, "languageWordListsDirectory", "languages");
		Deencapsulation.setField(checkerExt, "spellingIndexDirectoryPath", "./spellingIndex");
		checkerExt.initialize();

		// Create the index with multiple fields
		File indexDirectory = new File(INDEX_DIRECTORY_PATH);
		FileUtilities.deleteFile(indexDirectory, 1);
		FileUtilities.getFile(INDEX_DIRECTORY_PATH, Boolean.TRUE);

		Directory directory = FSDirectory.open(indexDirectory);

		IndexWriter indexWriter = new IndexWriter(directory, IConstants.ANALYZER, true, MaxFieldLength.UNLIMITED);
		int numDocs = 50;
		for (int i = 0; i < numDocs; i++) {
			for (final String string : strings) {
				String id = Integer.toString(i * 100);
				String contents = new StringBuilder("Hello world. " + string).append(i).toString();

				Document document = new Document();
				IndexManager.addStringField(IConstants.ID, id, document, Store.YES, Index.ANALYZED, TermVector.YES);
				if (StringUtilities.isNumeric(string.trim())) {
					IndexManager.addNumericField(IConstants.CONTENTS, string.trim(), document, Store.YES);
				} else {
					IndexManager.addStringField(IConstants.CONTENTS, contents, document, Store.YES, Index.ANALYZED, TermVector.YES);
				}
				IndexManager.addStringField(IConstants.NAME, "michael couck. " + string, document, Store.YES, Index.ANALYZED,
						TermVector.YES);
				indexWriter.addDocument(document);
			}
		}

		indexWriter.commit();
		indexWriter.forceMerge(5, Boolean.TRUE);
		indexWriter.close();

		Searchable[] searchables = new Searchable[] { new IndexSearcher(directory) };
		SEARCHER = new MultiSearcher(searchables);

		QueryParser queryParser = new QueryParser(IConstants.VERSION, IConstants.CONTENTS, IConstants.ANALYZER);
		Query query = queryParser.parse(russian);
		TopDocs topDocs = SEARCHER.search(query, 10);
		System.out.println("Total hits : " + topDocs.totalHits);

		query = queryParser.parse("michael");
		topDocs = SEARCHER.search(query, 10);
		System.out.println("Total hits : " + topDocs.totalHits);
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (SEARCHER != null) {
			SEARCHER.close();
		}
		FileUtilities.deleteFile(new File(INDEX_DIRECTORY_PATH), 1);
		Mockit.tearDownMocks(ReaderUtil.class);
	}

	private int maxResults = 10;

	public SearchTest() {
		super(SearchTest.class);
	}

	@Test
	public void searchSingle() {
		SearchSingle searchSingle = new SearchSingle(SEARCHER);
		searchSingle.setFirstResult(0);
		searchSingle.setFragment(Boolean.TRUE);
		searchSingle.setMaxResults(maxResults);
		searchSingle.setSearchField(IConstants.CONTENTS);
		searchSingle.setSearchString(russian);
		searchSingle.setSortField(new String[] { IConstants.ID });
		ArrayList<HashMap<String, String>> results = searchSingle.execute();
		assertTrue(results.size() > 1);
	}

	@Test
	public void searchMulti() {
		SearchMulti searchMulti = new SearchMulti(SEARCHER);
		searchMulti.setFirstResult(0);
		searchMulti.setFragment(Boolean.TRUE);
		searchMulti.setMaxResults(maxResults);
		searchMulti.setSearchField(IConstants.ID, IConstants.CONTENTS);
		searchMulti.setSearchString("id.1~", "hello");
		searchMulti.setSortField(new String[] { IConstants.ID });
		ArrayList<HashMap<String, String>> results = searchMulti.execute();
		assertTrue(results.size() > 1);
	}

	@Test
	public void searchMultiSorted() {
		SearchMultiSorted searchMultiSorted = new SearchMultiSorted(SEARCHER);
		searchMultiSorted.setFirstResult(0);
		searchMultiSorted.setFragment(Boolean.TRUE);
		searchMultiSorted.setMaxResults(maxResults);
		searchMultiSorted.setSearchField(IConstants.ID, IConstants.CONTENTS);
		searchMultiSorted.setSearchString("id.1~", "hello");
		searchMultiSorted.setSortField(new String[] { IConstants.ID });
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
	public void searchMultiAll() {
		SearchMultiAll searchMultiAll = new SearchMultiAll(SEARCHER);
		searchMultiAll.setFirstResult(0);
		searchMultiAll.setFragment(Boolean.TRUE);
		searchMultiAll.setMaxResults(maxResults);
		// searchMultiAll.setSearchField(IConstants.NAME);
		// searchMultiAll.setSortField(IConstants.ID);
		searchMultiAll.setSearchString("michael");
		ArrayList<HashMap<String, String>> results = searchMultiAll.execute();
		assertTrue(results.size() > 1);
	}

	@Test
	public void searchNumericAll() {
		SearchNumericAll searchNumericAll = new SearchNumericAll(SEARCHER);
		searchNumericAll.setFirstResult(0);
		searchNumericAll.setFragment(Boolean.TRUE);
		searchNumericAll.setMaxResults(10);
		searchNumericAll.setSearchField(IConstants.CONTENTS);
		searchNumericAll.setSearchString("123456790");
		ArrayList<HashMap<String, String>> results = searchNumericAll.execute();
		assertTrue(results.size() > 1);
	}

	@Test
	public void searchNumericRange() {
		SearchNumericRange searchNumericRange = new SearchNumericRange(SEARCHER);
		searchNumericRange.setFirstResult(0);
		searchNumericRange.setFragment(Boolean.TRUE);
		searchNumericRange.setMaxResults(10);
		searchNumericRange.setSearchField(IConstants.CONTENTS);
		searchNumericRange.setSearchString("123.456790", "123.456796");
		searchNumericRange.setSortField();
		ArrayList<HashMap<String, String>> results = searchNumericRange.execute();
		logger.info("Results : " + results);
		assertTrue(results.size() > 1);
	}

	@Test
	public void addStatistics() {
		String searchString = "michael AND couck";
		Search search = new SearchMultiAll(SEARCHER);
		search.setSearchString(searchString);

		ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
		search.addStatistics(results, 79, 23, null);
		Map<String, String> statistics = results.get(results.size() - 1);
		logger.info("Search strings : " + statistics.get(IConstants.SEARCH_STRINGS));
		logger.info("Corrected search strings : " + statistics.get(IConstants.CORRECTIONS));
		assertEquals("michael AND couck", statistics.get(IConstants.SEARCH_STRINGS));
		assertNotNull("Should be something like : michael and couch : ", statistics.get(IConstants.CORRECTIONS));
	}

	@Test
	public void getCorrections() {
		String[] searchStrings = { "some words", "are niet corect", "AND there are AND some words WITH AND another" };
		Search search = new SearchSingle(SEARCHER);
		search.setSearchString(searchStrings);
		String[] correctedSearchStrings = search.getCorrections();
		String correctedSearchString = Arrays.deepToString(correctedSearchStrings);
		logger.info("Corrected : " + correctedSearchString);
		assertEquals("Only the completely incorrect words should be replaced : ",
				"[and there are and some words with and another, are niet corect, some words]", correctedSearchString);
	}

}