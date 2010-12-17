package ikube;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ikube.database.IDataBase;
import ikube.index.parse.mime.MimeMapper;
import ikube.index.parse.mime.MimeTypes;
import ikube.logging.Logging;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.net.InetAddress;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class ATest {

	static {
		initialize();
	}

	protected static MultiSearcher MULTI_SEARCHER;
	protected static IndexSearcher INDEX_SEARCHER;
	protected static IndexReader INDEX_READER;
	protected static FSDirectory FS_DIRECTORY;
	protected static Searchable[] SEARCHABLES;
	protected static IndexWriter INDEX_WRITER;
	protected static Lock LOCK;
	protected static String IP;

	private static boolean INITIALIZED = Boolean.FALSE;

	protected Logger logger = Logger.getLogger(this.getClass());

	private static void initialize() {
		if (INITIALIZED) {
			return;
		}
		INITIALIZED = Boolean.TRUE;

		Logging.configure();
		new MimeTypes("/META-INF/mime/mime-types.xml");
		new MimeMapper("/META-INF/mime/mime-mapping.xml");

		MULTI_SEARCHER = mock(MultiSearcher.class);
		INDEX_SEARCHER = mock(IndexSearcher.class);
		INDEX_READER = mock(IndexReader.class);
		FS_DIRECTORY = mock(FSDirectory.class);
		SEARCHABLES = new Searchable[] { INDEX_SEARCHER };
		INDEX_WRITER = mock(IndexWriter.class);
		LOCK = mock(Lock.class);

		when(MULTI_SEARCHER.getSearchables()).thenReturn(SEARCHABLES);
		when(INDEX_SEARCHER.getIndexReader()).thenReturn(INDEX_READER);
		when(INDEX_READER.directory()).thenReturn(FS_DIRECTORY);
		when(FS_DIRECTORY.makeLock(anyString())).thenReturn(LOCK);

		try {
			IP = InetAddress.getLocalHost().getHostAddress();
			// Every time the JVM starts a +~JF#######.tmp file is created. Strange as that is
			// we still need to delete it manually.
			FileUtilities.deleteFiles(new File(System.getProperty("user.home")), ".tmp");
		} catch (Exception e) {
			e.printStackTrace();
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