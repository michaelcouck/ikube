package ikube.index.handler.internet;

import ikube.index.handler.Handler;
import ikube.index.handler.IHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableInternet;
import ikube.model.Url;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableInternetHandler extends Handler {

	public IndexableInternetHandler(IHandler<Indexable<?>> previous) {
		super(previous);
	}

	@Override
	public List<Thread> handle(final IndexContext indexContext, Indexable<?> indexable) throws Exception {
		if (IndexableInternet.class.isAssignableFrom(indexable.getClass())) {
			return handleInternet(indexContext, (IndexableInternet) indexable);
		}
		return new ArrayList<Thread>();
	}

	protected List<Thread> handleInternet(final IndexContext indexContext, final IndexableInternet indexableInternet) {
		List<Thread> threads = new ArrayList<Thread>();
		// The start url
		seedUrl(indexContext, indexableInternet);
		for (int i = 0; i < getThreads(); i++) {
			UrlHandler crawler = new UrlHandler(indexContext, indexableInternet, getDataBase(), threads);
			Thread thread = new Thread(crawler, this.getClass().getSimpleName() + "." + i);
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
		url.setName(indexableInternet.getName());
		url.setIndexed(Boolean.FALSE);

		indexContext.getCache().setUrl(url);
	}

}