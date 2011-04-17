package ikube.mock;

import java.io.IOException;

import mockit.Mock;
import mockit.MockClass;

import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;

@MockClass(realClass = MultiSearcher.class)
public class MultiSearcherMock {

	@Mock
	public MultiSearcherMock(Searchable... searchables) throws IOException {
	}
	
}
