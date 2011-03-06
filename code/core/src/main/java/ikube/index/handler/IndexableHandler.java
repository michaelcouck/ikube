package ikube.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;

/**
 * Base class for the handlers that contains access to common functionality like the threads etc.
 * 
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public abstract class IndexableHandler<T extends Indexable<?>> implements IHandler<T> {

	protected Logger logger = Logger.getLogger(this.getClass());

	/** The number of threads that this handler will spawn. */
	private int threads;
	/** The class that this handler can handle. */
	private Class<? extends Indexable<?>> indexableClass;

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public Class<? extends Indexable<?>> getIndexableClass() {
		return indexableClass;
	}

	public void setIndexableClass(Class<? extends Indexable<?>> indexableClass) {
		this.indexableClass = indexableClass;
	}

	@Override
	public void addDocument(IndexContext indexContext, Indexable<T> indexable, Document document) throws CorruptIndexException, IOException {
		indexContext.getIndex().getIndexWriter().addDocument(document);
	}

}
