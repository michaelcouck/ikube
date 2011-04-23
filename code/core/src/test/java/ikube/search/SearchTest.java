package ikube.search;

import static org.junit.Assert.*;
import ikube.ATest;
import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.index.spatial.Coordinate;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
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
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class SearchTest extends ATest {

	private static Searcher SEARCHER;

	@BeforeClass
	public static void beforeClass() throws Exception {
		// Create the index with multiple fields
		String indexDirectoryPath = "./indexes";
		File indexDirectory = new File(indexDirectoryPath);
		FileUtilities.deleteFile(indexDirectory, 1);
		FileUtilities.getFile(indexDirectoryPath, Boolean.TRUE);

		Directory directory = FSDirectory.open(indexDirectory);

		if (!IndexReader.indexExists(directory)) {
			IndexWriter indexWriter = new IndexWriter(directory, IConstants.ANALYZER, true, MaxFieldLength.UNLIMITED);
			int numDocs = 50;
			for (double i = 0; i < numDocs; i++) {

				String id = new StringBuilder("id.").append(Double.toString(Math.random() * i)).toString();
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
		SEARCHER.close();
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
		searchSingle.setSearchString("hello");
		searchSingle.setSortField(new String[] { IConstants.ID });
		List<Map<String, String>> results = searchSingle.execute();
		logger.info("Results : " + results);
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
		logger.info("Results : " + results);
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
		logger.info("Results : " + results);
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
	public void searchSpatial() {
		SearchSpatial searchSpatial = new SearchSpatial(SEARCHER);
		searchSpatial.setFirstResult(0);
		searchSpatial.setFragment(Boolean.TRUE);
		searchSpatial.setMaxResults(100);
		searchSpatial.setSearchField("content");
		searchSpatial.setSearchString("content");
		searchSpatial.setSortField("content");
		searchSpatial.setCoordinate(new Coordinate(51.0589216, 3.7243959));
		List<Map<String, String>> results = searchSpatial.execute();
		assertNotNull(results);
	}
}
