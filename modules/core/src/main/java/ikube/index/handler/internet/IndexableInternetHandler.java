package ikube.index.handler.internet;

import ikube.IConstants;
import ikube.index.handler.Handler;
import ikube.index.handler.IHandler;
import ikube.index.handler.internet.crawler.Crawler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableInternet;
import ikube.model.Url;

import java.util.ArrayList;
import java.util.List;

import org.apache.jcs.JCS;

/**
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableInternetHandler extends Handler {

	public static JCS IN;
	public static JCS OUT;
	public static JCS HASH;

	static {
		try {
			IN = JCS.getInstance(IConstants.IN);
			OUT = JCS.getInstance(IConstants.OUT);
			HASH = JCS.getInstance(IConstants.HASH);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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
		seedUrl(indexableInternet);
		for (int i = 0; i < getThreads(); i++) {
			Crawler crawler = new Crawler(indexContext, indexableInternet, getDataBase(), threads);
			Thread thread = new Thread(crawler, this.getClass().getSimpleName() + "." + i);
			threads.add(thread);
		}
		for (Thread thread : threads) {
			thread.start();
		}
		return threads;
	}

	protected void seedUrl(final IndexableInternet indexableInternet) {
		String urlString = indexableInternet.getUrl();
		indexableInternet.setCurrentUrl(urlString);

		Url url = new Url();
		url.setUrl(urlString);
		url.setName(indexableInternet.getName());
		url.setIndexed(Boolean.FALSE);

		try {
			IN.putInGroup(url.getUrl(), IConstants.URL, url);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}