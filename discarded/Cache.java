package ikube.cluster.cache.test;

import ikube.IConstants;
import ikube.model.Url;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;

public class Cache {

	private Logger logger;

	private JCS in;
	private JCS out;
	private JCS hash;
	private JCS all;

	private int batchSize = 100;
	private long waitTime = 100;

	public Cache() {
		this.logger = Logger.getLogger(Cache.class);
		initialise();
	}

	protected void initialise() {
		try {
			JCS.setConfigFilename("/cache.ccf");
			in = JCS.getInstance(IConstants.IN + "." + hashCode());
			out = JCS.getInstance(IConstants.OUT + "." + hashCode());
			hash = JCS.getInstance(IConstants.HASH + "." + hashCode());
			all = JCS.getInstance(IConstants.ALL + "." + hashCode());
		} catch (Exception e) {
			logger.error("Exception instantiating the cache : ", e);
		}
	}

	public synchronized List<Url> getUrlBatch(final List<Thread> threads) {
		Set<?> keys = in.getGroupKeys(IConstants.URL);
		if (keys.size() == 0) {
			for (Thread thread : threads) {
				if (thread.equals(Thread.currentThread())) {
					continue;
				}
				// Check that there is one thread still runnable which means
				// that there is a chance that there will be more urls found and
				// added to the cache. Once all the threads have gone into this
				// wait then the thread method will return null and the thread
				// will die. On exiting the method all the other now waiting threads
				// will waken, realize that there are not more active threads, exit
				// and them selves die, sé là vie, a tragedy, Russian fairy tale
				// where everyone dies in the end
				if (thread.getState().equals(State.RUNNABLE)) {
					// logger.debug(Logging.getString("Waiting : ", Thread.currentThread()));
					try {
						wait(waitTime);
					} catch (InterruptedException e) {
						logger.error("", e);
					}
					return getUrlBatch(threads);
				}
			}
		}
		// If we get here then there are two possibilities:
		// 1) There are urls in the list
		// 2) There are no results in the list and there are
		// no other threads that are still runnable
		List<Url> urls = new ArrayList<Url>();
		if (keys.size() > 0) {
			// Got some results, otherwise this thread is destined to die
			for (Object key : keys) {
				try {
					Url url = (Url) in.getFromGroup(key, IConstants.URL);
					url.setIndexed(Boolean.TRUE);
					urls.add(url);
					in.remove(key, IConstants.URL);
					out.putInGroup(key, IConstants.URL, url);
				} catch (Exception e) {
					logger.error("", e);
				}
				if (urls.size() >= batchSize) {
					break;
				}
			}
		}
		notifyAll();
		return urls;
	}

	public synchronized void setUrl(Url url) {
		try {
			Object object = all.getFromGroup(url.getUrl(), IConstants.URL);
			if (object != null) {
				return;
			}
			in.putInGroup(url.getUrl(), IConstants.URL, url);
			all.putInGroup(url.getUrl(), IConstants.URL, url);
		} catch (CacheException e) {
			logger.error("Exception setting the url in the cache : " + url, e);
		} finally {
			notifyAll();
		}
	}

	public synchronized Url getUrlWithHash(Url url) {
		try {
			Url fromCache = (Url) this.hash.getFromGroup(url.getHash(), IConstants.URL);
			if (fromCache == null) {
				this.hash.putInGroup(url.getHash(), IConstants.URL, url);
			}
			return fromCache;
		} catch (CacheException e) {
			logger.error("Exception setting the url in the cache : ", e);
		} finally {
			notifyAll();
		}
		return null;
	}

	public int getTotal(String group) {
		return this.all.getGroupKeys(group).size();
	}

	public synchronized void clear() {
		try {
			in.clear();
			out.clear();
			hash.clear();
			all.clear();
		} catch (Exception e) {
			logger.error("Exception clearing the cache : ", e);
			logger.error("Will try to re-initialise the cachees : ");
			initialise();
		}
	}

}