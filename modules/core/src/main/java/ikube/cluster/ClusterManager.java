package ikube.cluster;

import ikube.cluster.cache.ICache;
import ikube.logging.Logging;
import ikube.model.Batch;
import ikube.model.Server;
import ikube.model.Server.Action;
import ikube.model.Url;
import ikube.toolkit.HashUtilities;

import java.net.InetAddress;
import java.util.Iterator;
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

	protected static String URL_LOCK = "urlLock";
	protected static String BATCH_LOCK = "batchLock";
	protected static String SERVER_LOCK = "serverLock";
	protected static long LOCK_TIMEOUT = 3000;
	protected static double MAX_ACTION_SIZE = 10;
	protected static double ACTION_PRUNE_RATIO = 0.5;

	protected Logger logger;
	/** The address of this server. This can be set in the configuration. The default is the ip address. */
	protected String address;
	protected ICache cache;

	private ICache.ICriteria<Url> criteria = new ICache.ICriteria<Url>() {
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

	public ClusterManager() throws Exception {
		this.address = InetAddress.getLocalHost().getHostAddress();
	}

	public void initialise() throws Exception {
		this.logger = Logger.getLogger(this.getClass());
	}

	@Override
	public synchronized boolean anyWorking() {
		ILock lock = null;
		try {
			lock = lock(SERVER_LOCK);
			if (lock == null) {
				return Boolean.TRUE;
			}
			List<Server> servers = cache.get(Server.class, null, null, Integer.MAX_VALUE);
			for (Server server : servers) {
				if (server.getAddress().equals(this.address)) {
					continue;
				}
				if (server.isWorking()) {
					return Boolean.TRUE;
				}
			}
		} finally {
			unlock(lock);
			notifyAll();
		}
		return Boolean.FALSE;
	}

	/**
	 * TODO - Note: The batch id of the next row needs to be keyed on the indexable table not on the index name. For this we need to store
	 * the batch numbers for each of the tables that are indexed throughout the cluster in the action(s). The data hierarchy for the data is
	 * as follows:
	 * 
	 * <pre>
	 * Server => 
	 * 		1) Action => Index name => Action name => Handler name => Indexable name => Perhaps the id of the next row
	 * 		2) Action => Index name => Action name => Handler name => Indexable name => Perhaps the id of the next row
	 * 		3) Action => Index name => Action name => Handler name => Indexable name => Perhaps the id of the next row
	 * </pre>
	 * 
	 */
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
			Server server = cache.get(Server.class, HashUtilities.hash(address));
			if (server == null) {
				server = new Server();
				server.setAddress(address);
				server.setId(HashUtilities.hash(server.getAddress()));
				cache.set(Server.class, server.getId(), server);
			}
			return server;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized long setWorking(String indexName, String actionName, String handlerName, boolean isWorking) {
		// logger.info("Set working : ");
		ILock lock = null;
		try {
			lock = lock(SERVER_LOCK);
			if (lock == null) {
				return 0;
			}

			long lastStartTime = System.currentTimeMillis();
			List<Server> servers = cache.get(Server.class, null, null, Integer.MAX_VALUE);
			// Find the first start time for the action we want to start in any of the servers
			for (Server server : servers) {
				// logger.info("Server : " + server);
				for (Action action : server.getActions()) {
					if (indexName.equals(action.getIndexName()) && actionName.equals(action.getActionName()) && server.isWorking()) {
						lastStartTime = Math.min(lastStartTime, action.getStartTime());
					}
				}
			}

			Server server = getServer();

			// Prune the actions in this server
			List<Action> actions = server.getActions();
			if (actions.size() > MAX_ACTION_SIZE) {
				Iterator<Action> iterator = actions.iterator();
				double prunedSize = MAX_ACTION_SIZE * ACTION_PRUNE_RATIO;
				while (true) {
					/* Action action = */iterator.next();
					// logger.info("Removing action : " + action);
					iterator.remove();
					if (actions.size() <= prunedSize) {
						break;
					}
				}
			}

			server.setWorking(isWorking);
			server.getActions().add(server.new Action(handlerName, actionName, indexName, lastStartTime));

			// Publish the fact that this server is starting to work on an action
			// logger.info("Setting : " + server);
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
				acquired = lock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
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
				// logger.info(Logging.getString("Unlocked : ", lock, ", ", Thread.currentThread().hashCode()));
			}
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

	public void setAddress(String address) {
		this.address = address;
	}

}