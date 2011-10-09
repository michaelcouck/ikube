package ikube.mock;

import java.io.IOException;

import mockit.Mock;
import mockit.MockClass;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;

@MockClass(realClass = IndexWriter.class)
public class IndexWriterMock {

	public static boolean	IS_LOCKED;

	@Mock
	public static boolean isLocked(Directory directory) throws IOException {
		return IS_LOCKED;
	}

	public static void setIsLocked(boolean isLocked) {
		IndexWriterMock.IS_LOCKED = isLocked;
	}

}
