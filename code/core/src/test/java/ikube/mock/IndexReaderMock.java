package ikube.mock;

import static org.mockito.Mockito.mock;
import ikube.action.index.IndexManager;

import java.io.IOException;

import mockit.Mock;
import mockit.MockClass;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;

@MockClass(realClass = IndexReader.class)
public class IndexReaderMock {

	public static boolean INDEX_EXISTS;
	public static IndexReader INDEX_READER;

	@Mock
	public static boolean indexExists(Directory directory) throws IOException {
		return INDEX_EXISTS;
	}

	@Mock
	public static IndexReader open(final Directory directory, boolean readOnly) throws CorruptIndexException, IOException {
		return mock(IndexReader.class);
	}

	@Mock
	public static IndexReader open(final Directory directory) throws CorruptIndexException, IOException {
		return INDEX_READER;
	}

	@Mock
	protected final void ensureOpen() throws AlreadyClosedException {
		// Do nothing
	}

	@Mock
	public final Document document(int n) throws CorruptIndexException, IOException {
		return getDocument();
	}

	private Document getDocument() {
		Document document = new Document();
		IndexManager.addStringField("path", Long.toString(RandomUtils.nextLong()), document, Store.YES, Index.ANALYZED, TermVector.NO);
		IndexManager.addStringField("length", Long.toString(RandomUtils.nextLong()), document, Store.YES, Index.ANALYZED, TermVector.NO);
		IndexManager.addStringField("last-modified", Long.toString(RandomUtils.nextLong()), document, Store.YES, Index.ANALYZED,
				TermVector.NO);
		return document;
	}

	public static void setIndexExists(boolean indexExists) {
		IndexReaderMock.INDEX_EXISTS = indexExists;
	}
}
