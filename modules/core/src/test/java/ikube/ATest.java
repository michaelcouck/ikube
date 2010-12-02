package ikube;

import static org.mockito.Mockito.mock;
import ikube.database.IDataBase;
import ikube.index.parse.mime.MimeMapper;
import ikube.index.parse.mime.MimeTypes;
import ikube.logging.Logging;

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
		Logging.configure();
		new MimeTypes("/META-INF/mime/mime-types.xml");
		new MimeMapper("/META-INF/mime/mime-mapping.xml");
	}

	protected Logger logger = Logger.getLogger(this.getClass());

	protected MultiSearcher multiSearcher = mock(MultiSearcher.class);
	protected IndexSearcher indexSearcher = mock(IndexSearcher.class);
	protected IndexReader indexReader = mock(IndexReader.class);
	protected FSDirectory fsDirectory = mock(FSDirectory.class);
	protected Searchable[] searchables = new Searchable[] { indexSearcher };
	protected IndexWriter indexWriter = mock(IndexWriter.class);
	protected Lock lock = mock(Lock.class);
	protected String ip;
	{
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			logger.error("127.0.0.1 is best", e);
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

	protected void waitForThreads(List<Thread> threads) {
		outer: while (true) {
			for (Thread thread : threads) {
				if (thread.isAlive()) {
					try {
						thread.join();
					} catch (InterruptedException e) {
						logger.error("Interrupted waiting for thread : " + thread + ", this thread : " + Thread.currentThread(), e);
					}
					continue outer;
				}
			}
			break;
		}
	}

}
