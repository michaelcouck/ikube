package ikube.mock;

import ikube.index.IndexManager;
import ikube.model.IndexContext;
import mockit.Mock;
import mockit.MockClass;

import org.apache.lucene.index.IndexWriter;

/**
 * This mock is for the index manager that opens indexes on the file system, which we want to avoid.
 * 
 * @author Michael Couck
 * @since 20.03.11
 * @version 01.00
 */
@MockClass(realClass = IndexManager.class)
public class IndexManagerMock {
	
	public static IndexWriter INDEX_WRITER;

	@Mock()
	public static synchronized IndexWriter openIndexWriter(final String ip, final IndexContext indexContext, final long time) {
		return INDEX_WRITER;
	}

}
