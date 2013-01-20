package ikube.index.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.mock.SpellingCheckerMock;
import ikube.search.SearchSingle;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.NumericUtils;
import org.junit.After;
import org.junit.Before;
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

	private String russian = " русский язык  ";
	private String german = "Produktivität";
	private String french = "productivité";
	private String somethingElseAlToGether = "Soleymān Khāţer";
	private String string = "Qu'est ce qui détermine la productivité, et comment est-il mesuré? " //
			+ "Was bestimmt die Produktivität, und wie wird sie gemessen? " //
			+ "русский язык " + //
			"Soleymān Khāţer Solţānābād " + //
			russian + " " + //
			german + " " + //
			french + " " + //
			somethingElseAlToGether + " ";
	private String somethingNumeric = " 123456789 ";

	public LuceneTest() {
		super(LuceneTest.class);
	}

	@Before
	public void before() {
		new ThreadUtilities().initialize();
		Mockit.setUpMock(SpellingCheckerMock.class);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void search() throws Exception {
		SearchSingle searchSingle = createIndexAndSearch(SearchSingle.class, IConstants.ANALYZER, IConstants.CONTENTS, russian, german,
				french, somethingElseAlToGether, string, somethingNumeric);
		searchSingle.setFirstResult(0);
		searchSingle.setFragment(true);
		searchSingle.setMaxResults(10);
		searchSingle.setSearchField(IConstants.CONTENTS);

		ArrayList<HashMap<String, String>> results = null;

		searchSingle.setSearchString(french);
		results = searchSingle.execute();
		assertEquals(3, results.size());

		searchSingle.setSearchString(german + "~");
		results = searchSingle.execute();
		assertEquals(4, results.size());

		searchSingle.setSearchString(russian + "~");
		results = searchSingle.execute();
		assertEquals(3, results.size());

		searchSingle.setSearchString(somethingElseAlToGether);
		results = searchSingle.execute();
		assertEquals(3, results.size());
	}

	@Test
	public void concurrentReadAndWriteToIndex() {
		final long sleep = 100;
		final int iterations = 3;
		long time = System.currentTimeMillis();
		final File indexDirectory = createIndex(indexContext, time, "127.0.0.1", "the", "quick", "brown", "fox", "jumped");
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (int i = 0; i < 3; i++) {
			Future<?> future = ThreadUtilities.submit(Integer.toHexString(i), new Runnable() {
				@Override
				public void run() {
					try {
						int index = iterations * 2;
						while (index-- > 0) {
							ThreadUtilities.sleep(sleep * 10);

							Directory directory = FSDirectory.open(indexDirectory);
							IndexReader reader = IndexReader.open(directory);
							IndexSearcher indexSearcher = new IndexSearcher(reader);

							SearchSingle searchSingle = new SearchSingle(indexSearcher);
							searchSingle.setFirstResult(0);
							searchSingle.setFragment(Boolean.TRUE);
							searchSingle.setMaxResults(Integer.MAX_VALUE);
							searchSingle.setSearchField(IConstants.CONTENTS);
							searchSingle.setSearchString("détermine");
							searchSingle.setSortField(IConstants.CONTENTS);
							ArrayList<HashMap<String, String>> results = searchSingle.execute();
							assertTrue("There should be four results because the writer added three hits : ", results.size() >= 2);

							indexSearcher.close();
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
			futures.add(future);
		}
		Future<?> future = ThreadUtilities.submit(new Runnable() {
			public void run() {
				try {
					int index = iterations;
					while (index-- > 0) {
						IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, indexDirectory, Boolean.FALSE);
						Document document = getDocument(Long.toHexString(System.currentTimeMillis()), string, Index.ANALYZED);
						indexWriter.addDocument(document);
						indexWriter.commit();
						indexWriter.close(Boolean.TRUE);
						ThreadUtilities.sleep(sleep);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		futures.add(future);
		ThreadUtilities.waitForFutures(futures, 10000);
	}

	@Test
	public void searcherManagerReadAndWrite() throws IOException {
		final long sleep = 100;
		final int iterations = 3;
		long time = System.currentTimeMillis();
		final IndexWriter indexWriter = createIndexReturnWriter(indexContext, time, "127.0.0.1", "the", "quick", "brown", "fox", "jumped");
		final SearcherManager searcherManager = new SearcherManager(indexWriter, true, new SearcherFactory());
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (int i = 0; i < 3; i++) {
			Future<?> future = ThreadUtilities.submit(Integer.toHexString(i), new Runnable() {
				@Override
				public void run() {
					try {
						int index = iterations * 2;
						while (index-- > 0) {
							ThreadUtilities.sleep(sleep * 10);

							IndexSearcher indexSearcher = searcherManager.acquire();

							SearchSingle searchSingle = new SearchSingle(indexSearcher);
							searchSingle.setFirstResult(0);
							searchSingle.setFragment(Boolean.TRUE);
							searchSingle.setMaxResults(Integer.MAX_VALUE);
							searchSingle.setSearchField(IConstants.CONTENTS);
							searchSingle.setSearchString("détermine");
							searchSingle.setSortField(IConstants.CONTENTS);
							ArrayList<HashMap<String, String>> results = searchSingle.execute();
							assertTrue("There should be four results because the writer added three hits : ", results.size() >= 2);

							searcherManager.release(indexSearcher);
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
			futures.add(future);
		}
		Future<?> future = ThreadUtilities.submit(new Runnable() {
			public void run() {
				try {
					int index = iterations;
					while (index-- > 0) {
						Document document = getDocument(Long.toHexString(System.currentTimeMillis()), string, Index.ANALYZED);
						indexWriter.addDocument(document);
						indexWriter.commit();
						// indexWriter.close(Boolean.TRUE);
						searcherManager.maybeRefresh();
						ThreadUtilities.sleep(sleep);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		futures.add(future);
		ThreadUtilities.waitForFutures(futures, 10000);
	}

	@Test
	public void numeric() throws Exception {
		// Doesn't work
		// new QueryParser(IConstants.VERSION, IConstants.CONTENTS, IConstants.ANALYZER).parse(somethingNumeric.trim())

		File serverDirectory = createIndex(indexContext, russian, german, french, somethingElseAlToGether, string, somethingNumeric.trim());
		Directory directory = FSDirectory.open(serverDirectory);
		IndexReader indexReader = IndexReader.open(directory);
		printIndex(indexReader);

		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		TermQuery numberQuery = new TermQuery(new Term(IConstants.CONTENTS, NumericUtils.doubleToPrefixCoded(123456789L)));
		TopDocs topDocs = indexSearcher.search(numberQuery, 100);
		logger.info("Top docs : " + topDocs);
		for (final ScoreDoc scoreDoc : topDocs.scoreDocs) {
			logger.info("Score doc : " + scoreDoc);
		}
		assertEquals("There must be exactly one result from the search : ", 1, topDocs.scoreDocs.length);
	}

}