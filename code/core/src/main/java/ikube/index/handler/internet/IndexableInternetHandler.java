package ikube.index.handler.internet;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.cluster.cache.ICache;
import ikube.index.handler.IndexableHandler;
import ikube.index.handler.internet.crawler.IUrlHandler;
import ikube.index.handler.internet.crawler.UrlPageHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;

/**
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableInternetHandler extends IndexableHandler<IndexableInternet> {

	private List<Thread> threads;
	private transient IndexContext indexContext;

	public IndexableInternetHandler() {
		threads = new ArrayList<Thread>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Thread> handle(final IndexContext indexContext, final IndexableInternet indexable) throws Exception {
		if (isHandled(indexContext, indexable)) {
			return Arrays.asList();
		}
		threads.clear();
		this.indexContext = indexContext;
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		String name = this.getClass().getSimpleName();
		for (int i = 0; i < getThreads(); i++) {
			IUrlHandler<Url> urlPageHandler = new UrlPageHandler(clusterManager, this, indexable);
			threads.add(new Thread(urlPageHandler, name + "." + i));
		}
		// The start url
		seedUrl(indexable);
		for (Thread thread : threads) {
			thread.start();
		}
		return Arrays.asList(threads.toArray(new Thread[threads.size()]));
	}

	public boolean isCrawling() {
		int threadsRunnable = 0;
		for (Thread thread : threads) {
			if (thread.getState().equals(State.RUNNABLE)) {
				threadsRunnable++;
			}
		}
		if (threadsRunnable >= 2) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	protected void seedUrl(final IndexableInternet indexableInternet) {
		String urlString = indexableInternet.getUrl();
		indexableInternet.setCurrentUrl(urlString);

		Url url = new Url();
		url.setId(HashUtilities.hash(urlString));
		url.setUrl(urlString);
		url.setIndexed(Boolean.FALSE);

		ICache cache = ApplicationContextManager.getBean(ICache.class);
		cache.set(IConstants.URL, url.getId(), url);

		// IDataBase dataBase = ApplicationContextManager.getBean(DataBaseMem.class);
		// dataBase.persist(url);
	}

	@Override
	public void addDocument(IndexContext indexContext, Indexable<IndexableInternet> indexable, Document document)
			throws CorruptIndexException, IOException {
		if (this.indexContext == null) {
			logger.warn("Index context null, no handle was called then?");
			return;
		}
		super.addDocument(this.indexContext, indexable, document);
	}

	protected boolean isHandled(final IndexContext indexContext, final IndexableInternet indexableInternet) {
		return ApplicationContextManager.getBean(IClusterManager.class).isHandled(indexableInternet.getName(), indexContext.getIndexName());
	}

}