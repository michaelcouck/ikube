package ikube.cluster.cache;

import ikube.IConstants;
import ikube.model.Url;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jgroups.blocks.Cache.Value;
import org.jgroups.blocks.ReplCache;

public class JGroupsCache implements ICache<Url> {

	private Logger logger;

	/** Replicate to all nodes. */
	private short replications = -1;
	/** Never times out. */
	private long timeout = 0;
	/** The size of the batch to get. */
	private int batchSize = 100;
	/** The replicated cache/hash table. */
	private ReplCache<Long, Url> replCache;

	public JGroupsCache() {
		logger = Logger.getLogger(this.getClass());
		// IConstants.JGROUPS_CONFIGURATION_FILE
		replCache = new ReplCache<Long, Url>(null, IConstants.IKUBE);
		try {
			replCache.start();
		} catch (Exception e) {
			logger.error("Exception starting the cache : ", e);
		}
	}

	@Override
	public synchronized Url get(Long hash) {
		return replCache.get(hash);
	}

	@Override
	public synchronized void set(Long hash, Url url) {
		replCache.put(hash, url, replications, timeout);
	}

	@Override
	public synchronized List<Url> getBatch(Class<Url> urlClass, IAction<Url> action) {
		// Get a batch of urls that are not indexed yet
		List<Url> batch = new ArrayList<Url>();
		org.jgroups.blocks.Cache<Long, Url> l1Cache = replCache.getL1Cache();
		org.jgroups.blocks.Cache<Long, ReplCache.Value<Url>> l2Cache = replCache.getL2Cache();

		logger.info("L1 cache : " + l1Cache);
		logger.info("L2 cache : " + l2Cache);

		// logger.debug("L1 cache : " + l1Cache.getInternalMap());
		logger.debug("L2 cache : " + l2Cache.getInternalMap());

		Iterator<Map.Entry<Long, Value<ReplCache.Value<Url>>>> iterator = l2Cache != null ? l2Cache.entrySet().iterator() : null;
		// Iterator<Map.Entry<Long, Value<Url>>> iterator = l1Cache != null ? l1Cache.entrySet().iterator() : null;
		while (iterator.hasNext()) {
			Map.Entry<Long, Value<ReplCache.Value<Url>>> entry = iterator.next();
			Url url = entry.getValue().getValue().getVal();
			if (url.isIndexed()) {
				continue;
			}
			url.setIndexed(Boolean.TRUE);
			batch.add(url);
			if (batch.size() >= batchSize) {
				break;
			}
		}
		// Update all the other nodes
		for (Url url : batch) {
			set(url.getHash(), url);
		}
		return batch;
	}

	@Override
	public int size() {
		return replCache.getL1Cache().getSize();
	}

	@Override
	public void clear() {
		replCache.clear();
	}

}