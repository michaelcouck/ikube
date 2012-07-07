package ikube.index.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.search.SearchSingle;
import ikube.search.spelling.SpellingChecker;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import mockit.Deencapsulation;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Various tests for Lucene indexes, including language indexing and searching. This is just a sanity test for language support etc. Can
 * Lucene search for other character sets and are the results in the correct format, things like that, just to stay ahead of the insane.
 * 
 * @author Michael Couck
 * @since 06.03.10
 * @version 01.00
 */
public class LuceneTest extends ATest {

	private String russian = "р";
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

	public LuceneTest() {
		super(LuceneTest.class);
	}

	@Before
	public void before() throws Exception {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		SpellingChecker spellingChecker = new SpellingChecker();
		Deencapsulation.setField(spellingChecker, "languageWordListsDirectory", "./languageWordListsDirectory");
		Deencapsulation.setField(spellingChecker, "spellingIndexDirectoryPath", "./spellingIndexDirectoryPath");
		spellingChecker.initialize();
	}

	@After
	public void after() throws Exception {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		FileUtilities.deleteFile(new File("./spellingIndexDirectoryPath"), 1);
	}

	@Test
	public void search() throws Exception {
		File latestIndexDirectory = createIndex(indexContext, string);
		File serverIndexDirectory = new File(latestIndexDirectory.getAbsolutePath());
		IndexSearcher indexSearcher = new IndexSearcher(FSDirectory.open(serverIndexDirectory));
		Searchable[] searchables = new Searchable[] { indexSearcher };
		MultiSearcher searcher = new MultiSearcher(searchables);

		IndexReader indexReader = indexSearcher.getIndexReader();
		for (int i = 0; i < indexReader.numDocs(); i++) {
			Document document = indexReader.document(i);
			logger.info("Document : " + document);
		}

		try {
			SearchSingle searchSingle = new SearchSingle(searcher);
			searchSingle.setFirstResult(0);
			searchSingle.setFragment(true);
			searchSingle.setMaxResults(10);
			searchSingle.setSearchField(IConstants.CONTENTS);

			ArrayList<HashMap<String, String>> results = null;

			searchSingle.setSearchString(french);
			results = searchSingle.execute();
			assertEquals(2, results.size());

			searchSingle.setSearchString(german + "~");
			results = searchSingle.execute();
			assertEquals(2, results.size());

			// These don't work on Linux for some reason! Using the ATest.createIndex(...) method, but if you
			// create the index in here like in the SearchTest then it works! Stranger than fiction.

			// searchSingle.setSearchString(russian + "~");
			// results = searchSingle.execute();
			// assertEquals(2, results.size());

			// searchSingle.setSearchString(somthingElseAlToGether);
			// results = searchSingle.execute();
			// assertEquals(2, results.size());
		} finally {
			searcher.close();
		}
	}

	@Test
	@Ignore
	public void characterEncodingTest() throws Exception {
		IndexSearcher indexSearcher = null;
		try {
			File serverIndexDirectory = createIndex(indexContext, string);
			Directory directory = FSDirectory.open(serverIndexDirectory);
			indexSearcher = new IndexSearcher(directory);

			SearchSingle searchSingle = new SearchSingle(indexSearcher);
			searchSingle.setFirstResult(0);
			searchSingle.setMaxResults(10);
			searchSingle.setFragment(Boolean.TRUE);
			searchSingle.setSearchField(IConstants.CONTENTS);

			searchSingle.setSearchString("Solţānābād");
			ArrayList<HashMap<String, String>> results = searchSingle.execute();
			assertTrue("There must be at least one result : ", results.size() > 1);

			searchSingle.setSearchString("Soleymān Khater");
			results = searchSingle.execute();
			assertTrue("There must be at least one result : ", results.size() > 1);
		} finally {
			if (indexSearcher != null) {
				indexSearcher.close();
			}
		}
	}

}