package ikube.cluster;

import ikube.cluster.cache.ICache;
import ikube.cluster.cache.ICache.ICriteria;
import ikube.logging.Logging;
import ikube.model.Batch;
import ikube.model.Server;
import ikube.model.Url;
import ikube.toolkit.HashUtilities;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ILock;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ClusterManager implements IClusterManager {

	protected static long lockTimeout = 3000;
	protected static String URL_LOCK = "urlLock";
	protected static String BATCH_LOCK = "batchLock";
	protected static String SERVER_LOCK = "serverLock";

	protected Logger logger;
	protected Server server;
	protected ICache cache;

	private ICache.ICriteria<Url> criteria = new ICriteria<Url>() {
		@Override
		public boolean evaluate(Url t) {
			return !t.isIndexed();
		}
	};
	private ICache.IAction<Url> action = new ICache.IAction<Url>() {
		@Override
		public void execute(Url url) {
			url.setIndexed(Boolean.TRUE);
			cache.set(Url.class, url.getId(), url);
		}
	};

	public void initialise() throws Exception {
		this.logger = Logger.getLogger(this.getClass());
		if (this.server == null) {
			this.server = new Server();
			this.server.setAddress(InetAddress.getLocalHost().getHostAddress());
			this.server.setId(HashUtilities.hash(server.getAddress()));
		}
	}

	@Override
	public synchronized boolean anyWorking(String actionName) {
		try {
			List<Server> servers = cache.get(Server.class, null, null, Integer.MAX_VALUE);
			for (Server server : servers) {
				if (server.getAddress().equals(this.server.getAddress())) {
					continue;
				}
				if (server.isWorking() && actionName.equals(server.getAction().getActionName())) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized boolean areWorking(String indexName, String actionName) {
		try {
			List<Server> servers = cache.get(Server.class, null, null, Integer.MAX_VALUE);
			for (Server server : servers) {
				if (server.getAddress().equals(this.server.getAddress())) {
					continue;
				}
				if (indexName.equals(server.getAction().getIndexName()) && actionName.equals(server.getAction().getActionName())
						&& server.isWorking()) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized long getIdNumber(String indexName, long batchSize) {
		ILock lock = null;
		try {
			lock = lock(BATCH_LOCK);
			if (lock == null) {
				return 0;
			}
			Long id = HashUtilities.hash(indexName);
			Batch batch = cache.get(Batch.class, id);
			if (batch == null) {
				batch = new Batch();
				batch.setId(id);
				batch.setIndexName(indexName);
				batch.setIdNumber(new Long(0));
			}
			long idNumber = batch.getIdNumber();
			batch.setIdNumber(idNumber + batchSize);
			cache.set(Batch.class, id, batch);
			return idNumber;
		} finally {
			unlock(lock);
			notifyAll();
		}
	}

	@Override
	public synchronized Set<Server> getServers() {
		try {
			return new TreeSet<Server>(cache.get(Server.class, null, null, Integer.MAX_VALUE));
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized Server getServer() {
		try {
			return server;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized long setWorking(String indexName, String actionName, String handlerName, boolean isWorking) {
		ILock lock = null;
		try {
			lock = lock(SERVER_LOCK);
			if (lock == null) {
				return 0;
			}

			long lastStartTime = System.currentTimeMillis();
			List<Server> servers = cache.get(Server.class, null, null, Integer.MAX_VALUE);
			for (Server server : servers) {
				if (indexName == null || actionName == null) {
					continue;
				}
				if (indexName.equals(server.getAction().getIndexName()) && actionName.equals(server.getAction().getActionName())
						&& server.isWorking()) {
					lastStartTime = Math.min(lastStartTime, server.getAction().getStartTime());
				}
			}

			this.server.getAction().setIndexName(indexName);
			this.server.getAction().setActionName(actionName);
			this.server.getAction().setStartTime(lastStartTime);
			this.server.setWorking(isWorking);
			// Publish the fact that this server is starting to work on an action
			cache.set(Server.class, server.getId(), server);
			return lastStartTime;
		} finally {
			unlock(lock);
			notifyAll();
		}
	}

	private synchronized ILock lock(String lockName) {
		try {
			ILock lock = Hazelcast.getLock(lockName);
			boolean acquired = Boolean.FALSE;
			try {
				acquired = lock.tryLock(lockTimeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				logger.error("", e);
			}
			if (!acquired) {
				logger.warn(Logging.getString("Failed to acquire lock : ", lockName, ", ", Thread.currentThread().hashCode()));
				return null;
			}
			// logger.info(Logging.getString("Acquired lock : ", lock, ", ", Thread.currentThread().hashCode()));
			return lock;
		} finally {
			notifyAll();
		}
	}

	private synchronized void unlock(ILock lock) {
		try {
			if (lock != null) {
				lock.unlock();
			}
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized boolean anyWorkingOnIndex(String indexName) {
		try {
			List<Server> servers = cache.get(Server.class, null, null, Integer.MAX_VALUE);
			for (Server server : servers) {
				if (server.isWorking() && indexName.equals(server.getAction().getIndexName())) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized List<Url> getBatch(int size) {
		ILock lock = null;
		try {
			lock = lock(URL_LOCK);
			if (lock == null) {
				return null;
			}
			return cache.get(Url.class, criteria, action, size);
		} finally {
			unlock(lock);
			notifyAll();
		}
	}

	@Override
	public synchronized <T> T get(Class<T> klass, String sql) {
		try {
			return cache.get(klass, sql);
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized <T> void set(Class<T> klass, Long id, T t) {
		try {
			T exists = cache.get(klass, id);
			if (exists == null) {
				cache.set(klass, id, t);
			}
		} finally {
			notifyAll();
		}
	}

	public synchronized void setCache(ICache cache) {
		try {
			this.cache = cache;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized <T> void clear(Class<T> klass) {
		try {
			this.cache.clear(klass);
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized <T> int size(Class<T> klass) {
		try {
			return this.cache.size(klass);
		} finally {
			notifyAll();
		}
	}

	public void setServerAddress(String serverAddress) {
		this.server = new Server();
		this.server.setAddress(serverAddress);
		this.server.setId(HashUtilities.hash(server.getAddress()));
	}

}