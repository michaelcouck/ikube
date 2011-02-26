package ikube;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.index.parse.mime.MimeMapper;
import ikube.index.parse.mime.MimeTypes;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.model.Index;

import java.io.File;
import java.net.InetAddress;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class ATest {

	private static boolean INITIALIZED = Boolean.FALSE;
	
	static {
		if (!INITIALIZED) {
			INITIALIZED = Boolean.TRUE;
			try {
				new Initialiser().initialise();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/** These are all mocked objects that are used in sub classes. */
	protected static MultiSearcher MULTI_SEARCHER;
	protected static IndexSearcher INDEX_SEARCHER;
	protected static IndexReader INDEX_READER;
	protected static FSDirectory FS_DIRECTORY;
	protected static Searchable[] SEARCHABLES;
	protected static IndexWriter INDEX_WRITER;
	protected static TopDocs TOP_DOCS;
	protected static TopFieldDocs TOP_FIELD_DOCS;
	protected static ScoreDoc[] SCORE_DOCS;
	protected static Lock LOCK;
	protected static IndexContext INDEX_CONTEXT;
	protected static Index INDEX;

	protected static String IP;

	protected Logger logger = Logger.getLogger(this.getClass());

	static class Initialiser {
		protected void initialise() throws Exception {
			Logging.configure();
			new MimeTypes("/META-INF/mime/mime-types.xml");
			new MimeMapper("/META-INF/mime/mime-mapping.xml");

			MULTI_SEARCHER = mock(MultiSearcher.class);
			INDEX_SEARCHER = mock(IndexSearcher.class);
			INDEX_READER = mock(IndexReader.class);
			FS_DIRECTORY = mock(FSDirectory.class);
			SEARCHABLES = new Searchable[] { INDEX_SEARCHER };
			INDEX_WRITER = mock(IndexWriter.class);
			TOP_DOCS = mock(TopDocs.class);
			TOP_FIELD_DOCS = mock(TopFieldDocs.class);
			SCORE_DOCS = new ScoreDoc[0];
			LOCK = mock(Lock.class);
			INDEX_CONTEXT = mock(IndexContext.class);
			INDEX = mock(Index.class);
			
			when(MULTI_SEARCHER.getSearchables()).thenReturn(SEARCHABLES);
			when(MULTI_SEARCHER.search(any(Query.class), anyInt())).thenReturn(TOP_DOCS);
			when(MULTI_SEARCHER.search(any(Query.class), any(Filter.class), anyInt(), any(Sort.class))).thenReturn(TOP_FIELD_DOCS);
			when(INDEX_SEARCHER.getIndexReader()).thenReturn(INDEX_READER);
			when(INDEX_SEARCHER.search(any(Query.class), anyInt())).thenReturn(TOP_DOCS);
			TOP_DOCS.totalHits = 0;
			TOP_DOCS.scoreDocs = SCORE_DOCS;
			TOP_FIELD_DOCS.totalHits = 0;
			TOP_FIELD_DOCS.scoreDocs = SCORE_DOCS;
			when(INDEX_READER.directory()).thenReturn(FS_DIRECTORY);
			when(FS_DIRECTORY.makeLock(anyString())).thenReturn(LOCK);
			
			IP = InetAddress.getLocalHost().getHostAddress();
			// Every time the JVM starts a +~JF#######.tmp file is created. Strange as that is
			// we still need to delete it manually.
			// FileUtilities.deleteFiles(new File(System.getProperty("user.home")), ".tmp");
		}
	}

	protected static void delete(IDataBase dataBase, Class<?>... klasses) {
		for (Class<?> klass : klasses) {
			List<?> objects = dataBase.find(klass, 0, Integer.MAX_VALUE);
			for (Object object : objects) {
				dataBase.remove(object);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void quickSort(Comparable[] c, int start, int end) {
		if (end <= start) {
			return;
		}
		Comparable middle = c[start];
		int i = start;
		int j = end + 1;
		for (;;) {
			do {
				i++;
			} while (i < end && c[i].compareTo(middle) < 0);
			do {
				j--;
			} while (j > start && c[j].compareTo(middle) > 0);
			if (j <= i) {
				break;
			}
			Comparable smaller = c[i];
			Comparable larger = c[j];
			c[i] = larger;
			c[j] = smaller;
		}
		c[start] = c[j];
		c[j] = middle;
		quickSort(c, start, j - 1);
		quickSort(c, j + 1, end);
	}

	/**
	 * Returns the path to the latest index directory for this server and this context. The result will be something like
	 * './index/faq/1234567890/127.0.0.1'.
	 * 
	 * @param indexContext
	 *            the index context to get the directory path for
	 * @return the directory path to the latest index directory for this servers and context
	 */
	protected String getServerIndexDirectoryPath(IndexContext indexContext) {
		return IndexManager.getIndexDirectory(IP, indexContext, System.currentTimeMillis());
	}

	protected File createIndex(File indexDirectory) throws Exception {
		logger.info("Creating Lucene index in : " + indexDirectory);
		Directory directory = null;
		IndexWriter indexWriter = null;
		try {
			directory = FSDirectory.open(indexDirectory);
			indexWriter = new IndexWriter(directory, IConstants.ANALYZER, MaxFieldLength.UNLIMITED);
			Document document = new Document();
			document.add(new Field(IConstants.CONTENTS, "Michael Couck", Field.Store.YES, Field.Index.ANALYZED));
			indexWriter.addDocument(document);
			indexWriter.commit();
			indexWriter.optimize(Boolean.TRUE);
		} finally {
			try {
				directory.close();
			} finally {
				try {
					indexWriter.close();
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		return indexDirectory;
	}

	public static void main(String[] args) throws Exception {
		Integer[] arr = new Integer[5];
		System.out.println("inserting: ");
		for (int i = 0; i < arr.length; i++) {
			arr[i] = new Integer((int) (Math.random() * 99));
			System.out.print(arr[i] + " ");
		}
		quickSort(arr, 0, arr.length - 1);
		System.out.println("\nsorted: ");
		for (int i = 0; i < arr.length; i++)
			System.out.print(arr[i] + " ");
		System.out.println("\nDone ;-)");
	}

}