package ikube.mock;

import static org.mockito.Mockito.*;

import java.io.IOException;

import mockit.Mock;
import mockit.MockClass;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;

@MockClass(realClass = IndexReader.class)
public class IndexReaderMock {

	public static boolean INDEX_EXISTS;

	@Mock
	public static boolean indexExists(Directory directory) throws IOException {
		return INDEX_EXISTS;
	}

	@Mock
	public static IndexReader open(final Directory directory, boolean readOnly) throws CorruptIndexException, IOException {
		return mock(IndexReader.class);
	}
}
