package ikube.mock;

import java.io.IOException;

import mockit.Mock;
import mockit.MockClass;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

@MockClass(realClass = IndexSearcher.class)
public class IndexSearcherMock {

	@Mock
	@SuppressWarnings("unused")
	public IndexSearcherMock(IndexReader r) {
		// Documented
	}

	@Mock
	public int maxDoc() throws IOException {
		return Integer.MAX_VALUE;
	}

}
