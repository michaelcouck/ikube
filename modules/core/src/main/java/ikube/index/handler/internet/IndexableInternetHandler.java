package ikube.index.handler.internet;

import ikube.cluster.IClusterManager;
import ikube.index.handler.IndexableHandler;
import ikube.index.handler.IndexableHandlerType;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.SerializationUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles an internet resource, i.e. an URL. It acts as a starting point for the 'crawlers' which are essentially threads that
 * iterate over a site, either inter or intranet. A list of the threads are returned to the caller who is then obliged to wait for them to
 * finish.
 * 
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
		// The start url
		seedUrl(indexContext, indexable);
		String name = this.getClass().getSimpleName();
		for (int i = 0; i < getThreads(); i++) {
			IndexableInternet indexableClone = (IndexableInternet) SerializationUtilities.clone(indexable);
			IndexableInternetCrawler indexableInternetCrawler = new IndexableInternetCrawler(indexContext, indexableClone, threads);
			Thread thread = new Thread(indexableInternetCrawler, name + "." + i);
			thread.start();
			threads.add(thread);
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