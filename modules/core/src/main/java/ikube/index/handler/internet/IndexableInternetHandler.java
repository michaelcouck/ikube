package ikube.index.handler.internet;

import ikube.cluster.IClusterManager;
import ikube.index.handler.IndexableHandler;
import ikube.index.handler.IndexableHandlerType;
import ikube.index.handler.internet.crawler.PageHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.SerializationUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableInternetHandler extends IndexableHandler<IndexableInternet> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@IndexableHandlerType(type = IndexableInternet.class)
	public List<Thread> handle(final IndexContext indexContext, IndexableInternet indexable) throws Exception {
		List<Thread> threads = new ArrayList<Thread>();
		if (isHandled(indexContext, indexable)) {
			return threads;
		}

		String name = this.getClass().getSimpleName();
		for (int i = 0; i < getThreads(); i++) {
			IndexableInternet indexableInternet = (IndexableInternet) SerializationUtilities.clone(indexable);
			PageHandler pageHandler = new PageHandler(threads);
			pageHandler.setIndexContext(indexContext);
			pageHandler.setIndexableInternet(indexableInternet);
			threads.add(new Thread(pageHandler, name + "." + i));
		}
		// The start url
		seedUrl(indexable);
		for (Thread thread : threads) {
			thread.start();
		}
		return threads;
	}

	protected void seedUrl(IndexableInternet indexableInternet) {
		String urlString = indexableInternet.getUrl();
		indexableInternet.setCurrentUrl(urlString);

		Url url = new Url();
		url.setId(HashUtilities.hash(urlString));
		url.setUrl(urlString);
		url.setIndexed(Boolean.FALSE);

		PageHandler.IN_SET.add(url);

		// IDataBase dataBase = ApplicationContextManager.getBean(DataBaseMem.class);
		// dataBase.persist(url);
	}

	protected boolean isHandled(IndexContext indexContext, IndexableInternet indexableInternet) {
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		boolean isHandled = clusterManager.isHandled(indexableInternet.getName(), indexContext.getIndexName());
		return isHandled;
	}

}