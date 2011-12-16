package ikube.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class SearchTest extends ATest {

	private static Searcher	SEARCHER;
	private static String	INDEX_DIRECTORY_PATH	= "./" + SearchTest.class.getSimpleName();

	@BeforeClass
	public static void beforeClass() throws Exception {
		// Create the index with multiple fields
		File indexDirectory = new File(INDEX_DIRECTORY_PATH);
		FileUtilities.deleteFile(indexDirectory, 1);
		FileUtilities.getFile(INDEX_DIRECTORY_PATH, Boolean.TRUE);

		Directory directory = FSDirectory.open(indexDirectory);

		if (!IndexReader.indexExists(directory)) {
			IndexWriter indexWriter = new IndexWriter(directory, IConstants.ANALYZER, true, MaxFieldLength.UNLIMITED);
			int numDocs = 50;
			for (int i = 0; i < numDocs; i++) {
				String id = Integer.toString(i * 100);
				String contents = new StringBuilder("Hello world. ").append(i).toString();

				Document document = new Document();
				IndexManager.addStringField(IConstants.ID, id, document, Store.YES, Index.ANALYZED, TermVector.YES);
				IndexManager.addStringField(IConstants.CONTENTS, contents, document, Store.YES, Index.ANALYZED, TermVector.YES);
				IndexManager.addStringField(IConstants.NAME, "Michael Couck", document, Store.YES, Index.ANALYZED, TermVector.YES);
				indexWriter.addDocument(document);
			}

			indexWriter.commit();
			indexWriter.optimize();
			indexWriter.close();
		}

		Searchable[] searchables = new Searchable[] { new IndexSearcher(directory) };
		SEARCHER = new MultiSearcher(searchables);
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (SEARCHER != null) {
			SEARCHER.close();
		}
		FileUtilities.deleteFile(new File(INDEX_DIRECTORY_PATH), 1);
	}

	private int	maxResults	= 10;

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
		searchSingle.setSearchString("hello");
		searchSingle.setSortField(new String[] { IConstants.ID });
		List<Map<String, String>> results = searchSingle.execute();
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
		List<Map<String, String>> results = searchMulti.execute();
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
		List<Map<String, String>> results = searchMultiSorted.execute();
		assertTrue(results.size() > 1);

		// Verify that all the results are in ascending order according to the id
		String previousId = null;
		for (Map<String, String> result : results) {
			String id = result.get(IConstants.ID);
			logger.info("Previous id : " + previousId + ", id : " + id);
			if (previousId != null && id != null) {
				assertTrue(previousId.compareTo(id) < 0);
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
		searchMultiAll.setSearchField("content");
		searchMultiAll.setSearchString("Michael");
		searchMultiAll.setSortField("content");
		List<Map<String, String>> results = searchMultiAll.execute();
		assertTrue(results.size() > 1);
	}

	@Test
	public void addStatistics() {
		Search search = new SearchMultiAll(SEARCHER);
		search.setFirstResult(0);
		search.setFragment(Boolean.TRUE);
		search.setMaxResults(10);
		search.setSearchField("content");
		search.setSearchString("michael AND couck");
		search.setSortField("content");

		List<Map<String, String>> results = search.execute();
		search.addStatistics(results, 79, 23);
		Map<String, String> statistics = results.get(results.size() - 1);
		assertEquals("michael AND couck", statistics.get(IConstants.SEARCH_STRINGS));
		assertEquals("michael AND houck", statistics.get(IConstants.CORRECTIONS));
	}

	@Test
	@Ignore
	public void adHoc() throws Exception {
		String indexDirectoryName = "127.0.0.1";
		File indexDirectory = FileUtilities.findFileRecursively(new File("."), indexDirectoryName);
		Directory directory = FSDirectory.open(indexDirectory);
		IndexReader indexReader = IndexReader.open(directory);
		for (int i = 0; i < indexReader.numDocs(); i++) {
			Document document = indexReader.document(i);
			logger.error(document);
			List<Fieldable> fields = document.getFields();
			for (Fieldable fieldable : fields) {
				logger.error(fieldable);
			}
		}
	}

}
