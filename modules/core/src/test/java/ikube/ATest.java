package ikube;

import static org.mockito.Mockito.mock;
import ikube.index.parse.mime.MimeMapper;
import ikube.index.parse.mime.MimeTypes;
import ikube.logging.Logging;

import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;

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

	/**
	 * Returns the max read length byte array plus 1000, i.e. more than the max bytes that the application can read. This forces the indexer
	 * to get a reader rather than a string.
	 *
	 * @param string
	 *            the string to copy to the byte array until the max read length is exceeded
	 * @return the byte array of the string copied several times more than the max read length
	 */
	protected byte[] getBytes(String string) {
		byte[] bytes = new byte[(int) (IConstants.MAX_READ_LENGTH + IConstants.MAX_READ_LENGTH + 1000)];
		for (int offset = 0; offset < bytes.length;) {
			byte[] segment = string.getBytes();
			if (offset + segment.length >= bytes.length) {
				break;
			}
			System.arraycopy(segment, 0, bytes, offset, segment.length);
			offset += segment.length;
		}
		return bytes;
	}

}
