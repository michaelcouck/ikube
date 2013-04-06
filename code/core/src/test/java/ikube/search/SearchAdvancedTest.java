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
	private int maxResults = 10;
	// (cape AND town AND university) AND ("cape town") AND (one OR two OR three) NOT (caucasian)
	private String[] contentStrings = { //
	"cape town university", //
			"cape university town caucasian", //
			"cape one", "cape town", //
			"cape town university one" };

	public SearchAdvancedTest() {
		super(SearchAdvancedTest.class);
	}

	@Before
	public void before() throws Exception {
		Directory directory = new RAMDirectory();
		IndexWriter indexWriter = new IndexWriter(directory, IConstants.ANALYZER, true, MaxFieldLength.UNLIMITED);

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
		Mockit.tearDownMocks();
	}

	@Test
	public void searchAdvanced() throws Exception {
		SearchAdvanced searchAdvanced = new SearchAdvanced(searcher);

		searchAdvanced.setFirstResult(0);
		searchAdvanced.setFragment(Boolean.TRUE);
		searchAdvanced.setMaxResults(maxResults);
		searchAdvanced.setSearchField(IConstants.CONTENTS);
		searchAdvanced.setSearchString("all of these", "exact phrase", "any of these", "none of these");
		ArrayList<HashMap<String, String>> results = searchAdvanced.execute();
		assertEquals("There should be just a statistics map : ", 1, results.size());

		searchAdvanced.setSearchString("cape town university", "cape town", "one cape town university", "caucasian");
		results = searchAdvanced.execute();
		assertEquals("There should be two results and a statistics : ", 3, results.size());

		SearchSingle searchSingle = new SearchSingle(searcher);
		searchSingle.setFirstResult(0);
		searchSingle.setFragment(true);
		searchSingle.setMaxResults(10);
		searchSingle.setSearchField(IConstants.CONTENTS);
		searchSingle.setSearchString("cape town");
		searchSingle.setSortField(IConstants.CONTENTS);

		searchSingle
				.setSearchString("(cape AND town AND university) AND (\"cape town\") AND (one OR cape OR town OR university) NOT (caucasian)");
		results = searchSingle.execute();
		assertEquals("There should be two results and a statistics : ", 3, results.size());
	}
	
	@Test
	public void searchAdvancedAll() throws Exception {
		SearchAdvancedAll searchAdvancedAll = new SearchAdvancedAll(searcher);

		searchAdvancedAll.setFirstResult(0);
		searchAdvancedAll.setFragment(Boolean.TRUE);
		searchAdvancedAll.setMaxResults(maxResults);
		searchAdvancedAll.setSearchString("all of these", "exact phrase", "any of these", "none of these");
		ArrayList<HashMap<String, String>> results = searchAdvancedAll.execute();
		assertEquals("There should be just a statistics map : ", 1, results.size());

		searchAdvancedAll.setSearchString("cape town university", "cape town", "one cape town university", "caucasian");
		results = searchAdvancedAll.execute();
		assertEquals("There should be two results and a statistics : ", 3, results.size());

		SearchSingle searchSingle = new SearchSingle(searcher);
		searchSingle.setFirstResult(0);
		searchSingle.setFragment(true);
		searchSingle.setMaxResults(10);
		searchSingle.setSearchField(IConstants.CONTENTS);
		searchSingle.setSearchString("cape town");
		searchSingle.setSortField(IConstants.CONTENTS);

		searchSingle
				.setSearchString("(cape AND town AND university) AND (\"cape town\") AND (one OR cape OR town OR university) NOT (caucasian)");
		results = searchSingle.execute();
		assertEquals("There should be two results and a statistics : ", 3, results.size());
	}

}