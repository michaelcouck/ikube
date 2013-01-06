package ikube.action;

import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.Server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.index.IndexWriter;

/**
 * This class executes the handlers on the indexables, effectively creating the index. Each indexable has a handler that is implemented to
 * handle it. Each handler will return a list of threads that will do the indexing. The caller(in this case, this class) must then wait for
 * the threads to finish.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class Index extends AIndex {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	boolean executeInternal(final IndexContext<?> indexContext) throws Exception {
		Server server = clusterManager.getServer();
		List<Indexable<?>> indexables = indexContext.getIndexables();
		long startTime = System.currentTimeMillis();
		// Start the indexing for this server
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, startTime, server.getAddress());
		IndexWriter[] indexWriters = new IndexWriter[] { indexWriter };
		indexContext.setIndexWriters(indexWriters);
		Iterator<Indexable<?>> iterator = new ArrayList(indexables).iterator();
		executeIndexables(indexContext, iterator);
		return Boolean.TRUE;
	}

}