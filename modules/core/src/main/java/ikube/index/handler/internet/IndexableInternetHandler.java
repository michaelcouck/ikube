package ikube.index.handler.internet;

import ikube.cluster.IClusterManager;
import ikube.index.handler.IndexableHandler;
import ikube.index.handler.IndexableHandlerType;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableInternetHandler extends IndexableHandler<IndexableInternet> {

	@Override
	@IndexableHandlerType(type = IndexableInternet.class)
	public List<Thread> handle(final IndexContext indexContext, IndexableInternet indexable) throws Exception {
		return handleInternet(indexContext, indexable);
	}

	protected List<Thread> handleInternet(final IndexContext indexContext, final IndexableInternet indexableInternet) {
		List<Thread> threads = new ArrayList<Thread>();
		// The start url
		seedUrl(indexContext, indexableInternet);
		String name = this.getClass().getSimpleName();
		for (int i = 0; i < getThreads(); i++) {
			IndexableInternetCrawler indexableInternetCrawler = new IndexableInternetCrawler(indexContext, indexableInternet, threads);
			Thread thread = new Thread(indexableInternetCrawler, name + "." + i);
			threads.add(thread);
		}
		for (Thread thread : threads) {
			thread.start();
		}
		return threads;
	}

	protected void seedUrl(final IndexContext indexContext, final IndexableInternet indexableInternet) {
		String urlString = indexableInternet.getUrl();
		indexableInternet.setCurrentUrl(urlString);

		Url url = new Url();
		url.setUrl(urlString);
		url.setIndexed(Boolean.FALSE);
		url.setId(HashUtilities.hash(url.getUrl()));

		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.set(Url.class, url.getId(), url);
	}

}