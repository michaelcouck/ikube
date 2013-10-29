package ikube.search;

import static org.junit.Assert.assertEquals;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.mock.SpellingCheckerMock;

import java.util.ArrayList;
import java.util.HashMap;

import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 19.10.2012
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchAdvancedTest extends AbstractTest {

	private Searcher searcher;
	// (cape AND town AND university) AND ("cape town") AND (one OR two OR three) NOT (caucasian)
	private String[] contentStrings = { //
	"cape town university", //
			"cape university town caucasian", //
			"cape one", //
			"cape town", //
			"cape town university one" };

	@Before
	public void before() throws Exception {
		Directory directory = new RAMDirectory();
		IndexWriter indexWriter = new IndexWriter(directory, Search.ANALYZER, true, MaxFieldLength.UNLIMITED);

		for (final String contents : contentStrings) {
			Document document = new Document();
			String id = Long.toString(System.currentTimeMillis());
			IndexManager.addStringField(IConstants.ID, id, document, Store.YES, Index.ANALYZED, TermVector.YES);
			IndexManager.addStringField(IConstants.CONTENTS, contents, document, Store.YES, Index.ANALYZED, TermVector.YES);
			indexWriter.addDocument(document);
		}

		indexWriter.commit();
		indexWriter.optimize();
		indexWriter.close();

		searcher = new MultiSearcher(new IndexSearcher(directory));

		Mockit.setUpMock(SpellingCheckerMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(SpellingCheckerMock.class);
	}

	@Test
	public void searchAdvanced() throws Exception {
		SearchAdvanced searchAdvanced = new SearchAdvanced(searcher, Search.ANALYZER);
		searchAdvanced.setFirstResult(0);
		searchAdvanced.setFragment(Boolean.TRUE);
		searchAdvanced.setMaxResults(Integer.MAX_VALUE);
		searchAdvanced.setSearchField(IConstants.CONTENTS, IConstants.CONTENTS, IConstants.CONTENTS, IConstants.CONTENTS);

		// No results
		searchAdvanced.setSearchString("all of these", "exact phrase", "any of these", "none of these");
		ArrayList<HashMap<String, String>> results = searchAdvanced.execute();
		assertEquals("There should be just a statistics map : ", 1, results.size());

		// Simple query
		searchAdvanced.setSearchString("cape town university");
		results = searchAdvanced.execute();
		assertEquals("There should be all results and a statistics : ", 6, results.size());

		// Phrase query
		searchAdvanced.setSearchString(null, "cape town");
		results = searchAdvanced.execute();
		assertEquals("There should be three results and a statistics : ", 4, results.size());

		// TODO Review, at least one of these
		searchAdvanced.setSearchString("university", null, "eclipse");
		results = searchAdvanced.execute();
		assertEquals("There should be no results and a statistics : ", 4, results.size());

		searchAdvanced.setSearchString("university", null, "cape");
		results = searchAdvanced.execute();
		assertEquals("There should be no results and a statistics : ", 4, results.size());

		// Should not
		searchAdvanced.setSearchString("cape", null, null, "eclipse");
		results = searchAdvanced.execute();
		assertEquals("There should be no results and a statistics : ", 6, results.size());

		searchAdvanced.setSearchString("university", null, null, "caucasian");
		results = searchAdvanced.execute();
		assertEquals("There should be no results and a statistics : ", 3, results.size());
	}

}