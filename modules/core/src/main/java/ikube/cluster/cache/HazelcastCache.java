package ikube.cluster.cache;

import ikube.IConstants;
import ikube.model.Url;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hazelcast.core.Hazelcast;

public class HazelcastCache implements ICache<Url> {

	private int batchSize = 10;
	private Map<Long, Url> urls;

	public HazelcastCache() {
		urls = Hazelcast.getMap(IConstants.URL);
	}

	@Override
	public synchronized Url get(Long hash) {
		try {
			return urls.get(hash);
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized void set(Long hash, Url url) {
		try {
			boolean exists = urls.get(hash) != null;
			if (exists) {
				return;
			}
			urls.put(hash, url);
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized List<Url> getBatch(Class<Url> tClass, IAction<Url> action) {
		try {
			List<Url> batch = new ArrayList<Url>();
			for (Url url : urls.values()) {
				if (url.isIndexed()) {
					continue;
				}
				batch.add(url);
				if (batch.size() >= batchSize) {
					break;
				}
			}
			for (Url url : batch) {
				action.execute(url);
				urls.put(url.getHash(), url);
			}
			return batch;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized int size() {
		try {
			return urls.size();
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized void clear() {
		try {
			urls.clear();
		} finally {
			notifyAll();
		}
	}

}
