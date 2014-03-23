package ikube.mock;

import ikube.action.index.IndexManager;
import ikube.model.Indexable;
import mockit.Mock;
import mockit.MockClass;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.AlreadyClosedException;

import java.io.IOException;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
@MockClass(realClass = IndexReader.class)
public class IndexReaderMock {

    public static boolean INDEX_EXISTS;

    @Mock
    protected final void ensureOpen() throws AlreadyClosedException {
        // Do nothing
    }

    @Mock
    @SuppressWarnings("DuplicateThrows")
    public final Document document(int n) throws CorruptIndexException, IOException {
        return getDocument();
    }

    @Mock
    public final synchronized void close() throws IOException {
        // Do nothing
    }

    private Document getDocument() {
        Document document = new Document();
        Indexable indexable = new Indexable() {
        };
        IndexManager.addStringField("path", Long.toString(RandomUtils.nextLong()), indexable, document);
        IndexManager.addStringField("length", Long.toString(RandomUtils.nextLong()), indexable, document);
        IndexManager.addStringField("last-modified", Long.toString(RandomUtils.nextLong()), indexable, document);
        return document;
    }

    public static void setIndexExists(boolean indexExists) {
        IndexReaderMock.INDEX_EXISTS = indexExists;
    }
}
