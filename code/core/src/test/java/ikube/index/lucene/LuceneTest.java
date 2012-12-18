package ikube.index.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.mock.SpellingCheckerMock;
import ikube.search.SearchMultiAll;
import ikube.search.SearchSingle;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import mockit.Mockit;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
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

	private String russian = " русский язык  ";
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
	public void before() {
		Mockit.setUpMock(SpellingCheckerMock.class);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void search() throws Exception {
		SearchSingle searchSingle = createIndexAndSearch(SearchSingle.class, IConstants.ANALYZER, IConstants.CONTENTS, russian, german,
				french, somthingElseAlToGether, string);
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

		searchSingle.setSearchString(somthingElseAlToGether);
		results = searchSingle.execute();
		assertEquals(3, results.size());
	}

	@Test
	public void concurrentReadAndWriteToIndex() {
		final long sleep = 100;
		final int iterations = 3;
		long time = System.currentTimeMillis();
		final File indexDirectory = createIndex(indexContext, time, "127.0.0.1", "the", "quick", "brown", "fox", "jumped");
		logger.info("Index directories : " + indexDirectory);
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (int i = 0; i < 3; i++) {
			Future<?> future = ThreadUtilities.submit(Integer.toHexString(i), new Runnable() {
				@Override
				public void run() {
					try {
						int index = iterations * 2;
						while (index-- > 0) {
							ThreadUtilities.sleep(sleep);

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
							logger.info("Results : " + results.size());
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
						logger.info("Writing document : " + document);
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

	private String indexPath = "C:/media/nas/xfs-one/indexes/roma-streets/1355763362335/10.100.118.59";

	@Test
	@Ignore
	@SuppressWarnings("deprecation")
	public void adHocSearch() throws Exception {
		Directory directory = FSDirectory.open(new File(indexPath));
		IndexReader reader = IndexReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(reader);
		MultiSearcher multiSearcher = new MultiSearcher(indexSearcher);

		SearchMultiAll searchSingle = new SearchMultiAll(multiSearcher);
		searchSingle.setFirstResult(0);
		searchSingle.setFragment(Boolean.TRUE);
		searchSingle.setMaxResults(Integer.MAX_VALUE);
		searchSingle.setSearchField(IConstants.CONTENTS);
		searchSingle.setSearchString("sint amandstraat brussels brussel gent 9000");
		searchSingle.setSortField(IConstants.CONTENTS);
		ArrayList<HashMap<String, String>> results = searchSingle.execute();
		logger.info("Results : " + results);
	}

	@Test
	@Ignore
	public void adHocRead() throws Exception {
		Directory directory = FSDirectory.open(new File(indexPath));
		IndexReader indexReader = IndexReader.open(directory);
		System.out.println("Num docs : " + indexReader.numDocs());
		for (int i = 0; i < indexReader.numDocs(); i++) {
			Document document = indexReader.document(i);
			System.out.println("Document : " + document);
		}
		indexReader.close();
	}

}