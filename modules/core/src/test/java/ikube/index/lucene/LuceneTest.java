package ikube.index.lucene;

import static org.junit.Assert.assertEquals;

import ikube.BaseTest;
import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.search.SearchSingle;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Various tests for Lucene indexes, including language indexing and searching.
 *
 * @author Michael Couck
 * @since 06.03.10
 * @version 01.00
 */
public class LuceneTest extends BaseTest {

	private String russian = "определяет";
	private String german = "Produktivität";
	private String french = "productivité";
	private String string = "Qu'est ce qui détermine la productivité, et comment est-il mesuré? " //
			+ "Was bestimmt die Produktivität, und wie wird sie gemessen? " //
			+ "Что определяет производительность труда, и как ее измерить? ";

	@Before
	public void before() throws Exception {
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
		FileUtilities.deleteFile(latestIndexDirectory, 1);

		String ip = InetAddress.getLocalHost().getHostAddress();
		IndexWriter indexWriter = IndexManager.openIndexWriter(ip, indexContext, System.currentTimeMillis());
		Document document = new Document();
		Field field = new Field(IConstants.CONTENTS, string, Store.YES, Index.ANALYZED, TermVector.YES);
		document.add(field);
		indexWriter.addDocument(document, IConstants.ANALYZER);
		indexWriter.commit();
	}

	@After
	public void after() {
		File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
		FileUtilities.deleteFile(baseIndexDirectory, 1);
	}

	@Test
	public void search() throws Exception {
		IndexReader reader = indexContext.getIndexWriter().getReader();

		IndexSearcher indexSearcher = new IndexSearcher(reader);
		Searchable[] searchables = new Searchable[] { indexSearcher };
		MultiSearcher searcher = new MultiSearcher(searchables);

		SearchSingle searchSingle = new SearchSingle(searcher);
		searchSingle.setFirstResult(0);
		searchSingle.setFragment(true);
		searchSingle.setMaxResults(10);
		searchSingle.setSearchField(IConstants.CONTENTS);

		searchSingle.setSearchString(russian);
		List<Map<String, String>> results = searchSingle.execute();
		logger.warn(results);
		assertEquals(2, results.size());

		searchSingle.setSearchString(german);
		results = searchSingle.execute();
		logger.warn(results);
		assertEquals(2, results.size());

		searchSingle.setSearchString(french);
		results = searchSingle.execute();
		logger.warn(results);
		assertEquals(2, results.size());

		reader.close();
		searcher.close();
	}

}