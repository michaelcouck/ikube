package ikube.cluster;

import ikube.cluster.cache.ICache;
import ikube.model.Server;
import ikube.model.Server.Action;
import ikube.model.Url;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.Logging;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ILock;

/**
 * All the methods are synchronized in this class because not only is Ikube clusterable but also multi-threaded in each server.
 * 
 * @see IClusterManager
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ClusterManager implements IClusterManager {

	/** Lock objects for cluster wide locking while we update the cache. */
	protected static final String URL_LOCK = "urlLock";
	protected static final String SERVER_LOCK = "serverLock";

	/** The timeout to wait for the lock. */
	protected static final long LOCK_TIMEOUT = 3000;
	/** We only keep a few actions in the server. */
	protected static final double MAX_ACTION_SIZE = 100;
	/** The ratio to delete the actions when the maximum is reached. */
	protected static final double ACTION_PRUNE_RATIO = 0.5;

	protected static final Logger LOGGER = Logger.getLogger(ClusterManager.class);
	/** The ip of this server. */
	private transient String ip;
	/** The address of this server. This can be set in the configuration. The default is the IP address. */
	protected transient String address;
	/** The cluster wide cache. */
	protected transient ICache cache;

	/**
	 * This criteria is to check that the {@link Url} has already been indexed or not.
	 */
	private transient final ICache.ICriteria<Url> criteria = new ICache.ICriteria<Url>() {
		@Override
		public boolean evaluate(final Url url) {
			return !url.isIndexed();
		}
	};
	/**
	 * This action is to publish the {@link Url} while getting the next batch.
	 */
	private transient final ICache.IAction<Url> action = new ICache.IAction<Url>() {
		@Override
		public void execute(final Url url) {
			url.setIndexed(Boolean.TRUE);
			cache.set(Url.class.getName(), url.getId(), url);
		}
	};

	/**
	 * In the constructor we initialize the logger but most importantly the address of this server. Please see the comments.
	 * 
	 * @throws Exception
	 *             this can only be the InetAddress exception, which can never happen because we are looking for the localhost
	 */
	public ClusterManager() throws Exception {
		synchronized (this) {
			try {
				// We give each server a unique name because there can be several servers
				// started on the same machine. For example several Tomcats each with a war. In
				// this case the ip addresses will overlap. Ikube can also be started as stand alone
				// just in a Jvm(which is the same as a Tomcat essentially) also they will have the
				// same ip address
				this.ip = InetAddress.getLocalHost().getHostAddress();
				this.address = ip + "." + System.nanoTime();
			} finally {
				notifyAll();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
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
				if (server.getWorking()) {
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
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean anyWorking(final String indexName) {
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
				if (server.getWorking()) {
					for (Action action : server.getActions()) {
						if (action.getIndexName().equals(indexName)) {
							return Boolean.TRUE;
						}
					}
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
	public synchronized long getIdNumber(final String indexName, final String indexableName, final long batchSize, final long minId) {
		ILock lock = null;
		try {
			lock = lock(SERVER_LOCK);
			if (lock == null) {
				return 0;
			}
			long idNumber = 0;
			List<Server> servers = cache.get(Server.class.getName(), null, null, Integer.MAX_VALUE);
			// We look for the largest row id from any of the servers, from the first action in each server
			for (Server server : servers) {
				List<Action> actions = server.getActions();
				// Iterate from the end to the front
				for (int i = actions.size() - 1; i >= 0; i--) {
					Action action = actions.get(i);
					// logger.info("Action : " + action + ", server : " + server.getAddress());
					if (action.getIndexableName().equals(indexableName) && action.getIndexName().equals(indexName)) {
						if (action.getIdNumber() > idNumber) {
							idNumber = action.getIdNumber();
						}
						// We break when we get to the first action in this server
						break;
					}
				}
			}
			// We find the action for this server
			Server server = cache.get(Server.class.getName(), HashUtilities.hash(address));
			if (server == null) {
				server = getServer();
			}
			List<Action> actions = server.getActions();
			Action currentAction = null;

			for (int i = actions.size() - 1; i >= 0; i--) {
				Action action = actions.get(i);
				if (action.getIndexableName().equals(indexableName) && action.getIndexName().equals(indexName)) {
					currentAction = action;
					break;
				}
			}

			// This can never happen, can it?
			if (currentAction == null) {
				LOGGER.warn("Action null : " + indexName + ", " + indexableName + ", " + batchSize);
				currentAction = server.new Action();
				currentAction.setIndexableName(indexableName);
				currentAction.setIndexName(indexName);
				currentAction.setStartTime(System.currentTimeMillis());
				actions.add(currentAction);
			}
			if (idNumber < minId) {
				idNumber = minId;
			}
			long nextIdNumber = idNumber + batchSize;
			// Set the next row number to the current + the batch size
			currentAction.setIdNumber(nextIdNumber);
			// Publish the server to the cluster
			cache.set(Server.class.getName(), server.getId(), server);
			return idNumber;
		} finally {
			unlock(lock);
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized List<Server> getServers() {
		try {
			return cache.get(Server.class.getName(), null, null, Integer.MAX_VALUE);
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Server getServer() {
		try {
			Server server = cache.get(Server.class.getName(), HashUtilities.hash(address));
			if (server == null) {
				server = new Server();
				server.setIp(ip);
				server.setAddress(address);
				server.setId(HashUtilities.hash(address));
				cache.set(Server.class.getName(), server.getId(), server);
				LOGGER.info("Published server : " + server);
			}
			return server;
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized long setWorking(final String indexName, final String indexableName, final boolean isWorking) {
		// logger.info("Set working : ");
		ILock lock = null;
		try {
			lock = lock(SERVER_LOCK);
			if (lock == null) {
				return 0;
			}

			long firstStartTime = System.currentTimeMillis();
			List<Server> servers = cache.get(Server.class.getName(), null, null, Integer.MAX_VALUE);
			// Find the first start time for the action we want to start in any of the servers
			for (Server server : servers) {
				LOGGER.debug("Server : " + server);
				for (Action action : server.getActions()) {
					if (indexName.equals(action.getIndexName()) && indexableName.equals(action.getIndexableName()) && server.getWorking()) {
						firstStartTime = Math.min(firstStartTime, action.getStartTime());
					}
				}
			}

			// Set the server working and the new action in the list
			Server server = cache.get(Server.class.getName(), HashUtilities.hash(address));
			if (server == null) {
				server = getServer();
			}
			server.setWorking(isWorking);
			server.getActions().add(server.new Action(0, indexableName, indexName, firstStartTime));

			// Prune the actions in this server
			List<Action> actions = server.getActions();
			if (actions.size() > MAX_ACTION_SIZE) {
				Iterator<Action> iterator = actions.iterator();
				double prunedSize = MAX_ACTION_SIZE * ACTION_PRUNE_RATIO;
				while (iterator.hasNext()) {
					Action action = iterator.next();
					LOGGER.debug("Removing action : " + action);
					iterator.remove();
					if (actions.size() <= prunedSize) {
						break;
					}
				}
			}

			// Publish the fact that this server is starting to work on an action
			LOGGER.debug("Publishing server : " + server.getAddress());
			cache.set(Server.class.getName(), server.getId(), server);
			return firstStartTime;
		} finally {
			unlock(lock);
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean isHandled(String indexableName, String indexName) {
		try {
			Server thisServer = cache.get(Server.class.getName(), HashUtilities.hash(address));
			List<Server> servers = cache.get(Server.class.getName(), null, null, Integer.MAX_VALUE);
			for (Server server : servers) {
				if (server.getAddress().equals(thisServer.getAddress())) {
					continue;
				}
				for (Action action : server.getActions()) {
					if (action.getIndexName().equals(indexName) && action.getIndexableName().equals(indexableName)) {
						LOGGER.info("Already indexed by : " + server);
						return Boolean.TRUE;
					}
				}
			}
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	/**
	 * This method locks the object map. This is required to maintain data integrity in the cluster.
	 * 
	 * @param lockName
	 *            the name of the lock we want
	 * @return the lock for the object map or null if this lock is un-available
	 */
	public synchronized ILock lock(String lockName) {
		try {
			ILock lock = Hazelcast.getLock(lockName);
			boolean acquired = Boolean.FALSE;
			try {
				acquired = lock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				LOGGER.error("Interrupted acquiring lock for : " + lockName, e);
			}
			if (!acquired) {
				LOGGER.warn(Logging.getString("Failed to acquire lock : ", lockName, Thread.currentThread().hashCode()));
				return null;
			}
			return lock;
		} finally {
			notifyAll();
		}
	}

	/**
	 * This method unlocks the object map in the cluster.
	 * 
	 * @param lock
	 *            the lock to release
	 */
	public synchronized void unlock(ILock lock) {
		try {
			if (lock != null) {
				lock.unlock();
			}
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public synchronized <T> T get(Class<T> klass, String sql) {
		try {
			return (T) cache.get(klass.getName(), sql);
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public synchronized <T> T get(final Class<T> klass, final Long id) {
		try {
			return (T) cache.get(klass.getName(), id);
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> void set(final Class<T> klass, final Long id, final T object) {
		try {
			cache.set(klass.getName(), id, object);
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> void clear(Class<T> klass) {
		try {
			this.cache.clear(klass.getName());
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> int size(Class<T> klass) {
		try {
			return this.cache.size(klass.getName());
		} finally {
			notifyAll();
		}
	}

	/** Methods called form Spring below here. */
	public synchronized void setCache(ICache cache) {
		try {
			this.cache = cache;
		} finally {
			notifyAll();
		}
	}

	public synchronized void setAddress(String address) {
		try {
			this.address = address;
		} finally {
			notifyAll();
		}
	}

}