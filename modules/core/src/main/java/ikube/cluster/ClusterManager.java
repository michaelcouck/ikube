package ikube.cluster;

import ikube.cluster.cache.ICache;
import ikube.logging.Logging;
import ikube.model.Server;
import ikube.model.Server.Action;
import ikube.model.Url;
import ikube.toolkit.HashUtilities;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
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
			cache.set(Url.class.getName(), url.getId(), url);
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
			List<Server> servers = cache.get(Server.class.getName(), null, null, Integer.MAX_VALUE);
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
	 * <pre>
	 * Server => 
	 * 		Action =>
	 * 			Index name => 
	 * 			Indexable name => 
	 * 			Id number =>
	 * </pre>
	 */
	@Override
	public synchronized long getIdNumber(String indexName, String indexableName, long batchSize) {
		ILock lock = null;
		try {
			lock = lock(SERVER_LOCK);
			if (lock == null) {
				return 0;
			}
			long idNumber = 0;
			List<Server> servers = cache.get(Server.class.getName(), null, null, Integer.MAX_VALUE);
			// We look for the largest row id from any of the servers
			for (Server server : servers) {
				for (Action action : server.getActions()) {
					logger.info("Action : " + action + ", server : " + server.getAddress());
					if (action.getIndexableName().equals(indexableName) && action.getIndexName().equals(indexName)) {
						if (action.getIdNumber() > idNumber) {
							idNumber = action.getIdNumber();
						}
					}
				}
			}
			// We find the action for this server
			Server server = getServer();
			List<Action> actions = server.getActions();
			Action currentAction = null;
			for (Action action : actions) {
				if (action.getIndexableName().equals(indexableName) && action.getIndexName().equals(indexName)) {
					currentAction = action;
					break;
				}
			}
			if (currentAction == null) {
				currentAction = server.new Action();
				currentAction.setIndexableName(indexableName);
				currentAction.setIndexName(indexName);
				currentAction.setStartTime(System.currentTimeMillis());
				actions.add(currentAction);
			}
			// Set the next row number to the current + the batch size
			currentAction.setIdNumber(idNumber + batchSize);
			// Publish the server to the cluster
			cache.set(Server.class.getName(), server.getId(), server);
			return idNumber;
		} finally {
			unlock(lock);
			notifyAll();
		}
	}

	@Override
	public synchronized List<Server> getServers() {
		try {
			return cache.get(Server.class.getName(), null, null, Integer.MAX_VALUE);
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized Server getServer() {
		try {
			Server server = cache.get(Server.class.getName(), HashUtilities.hash(address));
			if (server == null) {
				server = new Server();
				server.setAddress(address);
				server.setId(HashUtilities.hash(server.getAddress()));
				cache.set(Server.class.getName(), server.getId(), server);
			}
			return server;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized long setWorking(String indexName, String indexableName, boolean isWorking) {
		// logger.info("Set working : ");
		ILock lock = null;
		try {
			lock = lock(SERVER_LOCK);
			if (lock == null) {
				return 0;
			}

			long lastStartTime = System.currentTimeMillis();
			List<Server> servers = cache.get(Server.class.getName(), null, null, Integer.MAX_VALUE);
			// Find the first start time for the action we want to start in any of the servers
			for (Server server : servers) {
				logger.info("Server : " + server);
				for (Action action : server.getActions()) {
					if (indexName.equals(action.getIndexName()) && indexableName.equals(action.getIndexableName()) && server.isWorking()) {
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
			server.getActions().add(server.new Action(0, indexableName, indexName, lastStartTime));

			// Publish the fact that this server is starting to work on an action
			// logger.info("Setting : " + server);
			cache.set(Server.class.getName(), server.getId(), server);
			return lastStartTime;
		} finally {
			unlock(lock);
			notifyAll();
		}
	}

	@Override
	public synchronized boolean isHandled(String indexableName, String indexName) {
		try {
			Server thisServer = getServer();
			List<Server> servers = getServers();
			for (Server server : servers) {
				if (server.equals(thisServer)) {
					continue;
				}
				for (Action action : server.getActions()) {
					if (action.getIndexName().equals(indexName) && action.getIndexableName().equals(indexableName)) {
						logger.info("File share already indexed by : " + server);
						return Boolean.TRUE;
					}
				}
			}
			return Boolean.FALSE;
		} finally {

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
			return cache.get(Url.class.getName(), criteria, action, size);
		} finally {
			unlock(lock);
			notifyAll();
		}
	}

	@Override
	public synchronized <T> T get(Class<T> klass, String sql) {
		try {
			return cache.get(klass.getName(), sql);
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized <T> void set(Class<T> klass, Long id, T t) {
		try {
			T exists = cache.get(klass.getName(), id);
			if (exists == null) {
				cache.set(klass.getName(), id, t);
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
			this.cache.clear(klass.getName());
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized <T> int size(Class<T> klass) {
		try {
			return this.cache.size(klass.getName());
		} finally {
			notifyAll();
		}
	}

	public void setAddress(String address) {
		this.address = address;
	}

}