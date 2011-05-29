package ikube.cluster;

import ikube.IConstants;
import ikube.cluster.cache.ICache;
import ikube.cluster.cache.ICache.IAction;
import ikube.cluster.cache.ICache.ICriteria;
import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.model.Server;
import ikube.model.Server.Action;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.Logging;

import java.io.File;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ILock;
import com.hazelcast.core.MessageListener;

/**
 * All the methods are synchronised in this class because not only is Ikube clusterable but also multi-threaded in each server.
 * 
 * @see IClusterManager
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ClusterManager implements IClusterManager, IConstants {

	/** The logger, doh. */
	protected static final Logger LOGGER = Logger.getLogger(ClusterManager.class);

	/**
	 * This method adds a shutdown hook that can be executed remotely causing the the cluster to close down, but not ourselves. This is
	 * useful when a unit test needs to run without the cluster running as the synchronization will affect the tests.
	 */
	public static void addShutdownHook() {
		LOGGER.info("Adding shutdown listener : ");
		Hazelcast.getTopic(IConstants.SHUTDOWN_TOPIC).addMessageListener(new MessageListener<Object>() {
			@Override
			public void onMessage(Object other) {
				if (other == null) {
					return;
				}
				LOGGER.info("Got shutdown message : " + other);
				Server server = ApplicationContextManager.getBean(IClusterManager.class).getServer();
				if (other.equals(server)) {
					// We don't shutdown our selves of course
					return;
				}
				LOGGER.warn("Shutting down Ikube server : " + other);
				Hazelcast.shutdownAll();
				System.exit(0);
			}
		});
	}

	/**
	 * This method adds a listener to the cluster topic to make exception when evaluating the rules.
	 */
	public static void addClusterExceptionListener() {
		// Add the listener for general cluster directives like forcing an index to start
		LOGGER.info("Adding shutdown listener : ");
		Hazelcast.getTopic(IConstants.EXCEPTION_TOPIC).addMessageListener(new MessageListener<Object>() {
			@Override
			public void onMessage(Object command) {
				LOGGER.info("Got exception message : " + command);
				if (Boolean.class.isAssignableFrom(command.getClass())) {
					LOGGER.info("Exception command not boolean : " + command);
					return;
				}
				ApplicationContextManager.getBean(IClusterManager.class).setException((Boolean) command);
			}
		});
	}

	/**
	 * This listener will respond to clean events and it will remove servers that have not checked in, i.e. their sell by date is expired.
	 */
	private IListener cleanerListener = new IListener() {
		@Override
		public void handleNotification(Event event) {
			if (!event.getType().equals(Event.CLEAN)) {
				return;
			}
			ILock lock = null;
			try {
				lock = AtomicAction.lock(SERVER_LOCK);
				// Remove all servers that are past the max age
				List<Server> servers = getServers();
				for (Server remoteServer : servers) {
					if (System.currentTimeMillis() - remoteServer.getAge() > MAX_AGE) {
						LOGGER.info("Removing server : " + remoteServer + ", "
								+ (System.currentTimeMillis() - remoteServer.getAge() > MAX_AGE));
						remove(Server.class.getName(), remoteServer.getId());
					}
				}
			} finally {
				if (lock != null) {
					AtomicAction.unlock(lock);
				}
			}
		}
	};

	/**
	 * This listener will reset this server, with the latest timestamp so we can stay in the server club.
	 */
	private IListener aliveListener = new IListener() {
		@Override
		public void handleNotification(Event event) {
			if (!event.getType().equals(Event.ALIVE)) {
				return;
			}
			ILock lock = null;
			try {
				lock = AtomicAction.lock(SERVER_LOCK);
				// Set our own server age
				Server server = getServer();
				// Add the tail end of the log to the server
				File logFile = Logging.getLogFile();
				if (logFile != null && logFile.exists() && logFile.canRead()) {
					String logTail = FileUtilities.getContentsFromEnd(logFile, 20000).toString();
					server.setLogTail(logTail);
				}
				server.setAge(System.currentTimeMillis());
				LOGGER.info("Publishing server : " + server);
				set(Server.class.getName(), server.getId(), server);
			} finally {
				AtomicAction.unlock(lock);
			}
		}
	};

	/** The ip of this server. */
	private transient String ip;
	/**
	 * The address of this server, this must be unique in the cluster. Typically this is the ip address added to the system time. The chance
	 * of a hash clash with any other server is in the billions, we will disregard this possibility on prudent grounds.
	 */
	protected transient String address;
	/** The cluster wide cache. */
	protected transient ICache cache;
	/** This flag is set cluster wide to make exception for the rules. */
	private transient boolean exception;

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
				// This listener will iterate over the servers and remove any that have expired
				// and will also register this server as still alive in the cluster
				ListenerManager.addListener(aliveListener);
				ListenerManager.addListener(cleanerListener);
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
			lock = AtomicAction.lock(SERVER_LOCK);
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
			AtomicAction.unlock(lock);
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
			lock = AtomicAction.lock(SERVER_LOCK);
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
			AtomicAction.unlock(lock);
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
			lock = AtomicAction.lock(SERVER_LOCK);
			if (lock == null) {
				return 0;
			}
			// TODO Have another look at this, surely this can be simplified, but be careful
			long idNumber = 0;
			List<Server> servers = cache.get(Server.class.getName(), null, null, Integer.MAX_VALUE);
			// We look for the largest row id from any of the servers, from the last action in each server
			// that has the name of the action that we are looking for
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
						// We break when we get to the first action in this server that
						// corresponds to the index that we are going to create and the
						// action that we want to execute
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
			AtomicAction.unlock(lock);
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
				server.setAge(System.currentTimeMillis());
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
			lock = AtomicAction.lock(SERVER_LOCK);
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
			AtomicAction.unlock(lock);
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
	 * TODO This method has been moved to the {@link AtomicAction} class, so once this is tested it can be removed.
	 * 
	 * This method locks the object map. This is required to maintain data integrity in the cluster.
	 * 
	 * @param lockName
	 *            the name of the lock we want
	 * @return the lock for the object map or null if this lock is un-available
	 */
	@Deprecated
	// public synchronized ILock lock(String lockName) {
	// try {
	// ILock lock = Hazelcast.getLock(lockName);
	// boolean acquired = Boolean.FALSE;
	// try {
	// acquired = lock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
	// } catch (InterruptedException e) {
	// LOGGER.error("Interrupted acquiring lock for : " + lockName, e);
	// }
	// if (!acquired) {
	// LOGGER.warn(Logging.getString("Failed to acquire lock : ", lockName, Thread.currentThread().hashCode()));
	// return null;
	// }
	// return lock;
	// } finally {
	// notifyAll();
	// }
	// }
	/**
	 * TODO This method has been moved to the {@link AtomicAction} class, so once this is tested it can be removed.
	 * 
	 * This method unlocks the object map in the cluster.
	 * 
	 * @param lock
	 *            the lock to release
	 */
	// @Deprecated
	// public synchronized void unlock(ILock lock) {
	// try {
	// if (lock != null) {
	// lock.unlock();
	// }
	// } finally {
	// notifyAll();
	// }
	// }
	@Override
	public synchronized <T> void set(String name, Long id, T object) {
		try {
			cache.set(name, id, object);
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> int size(String name) {
		try {
			return this.cache.size(name);
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized <T> void clear(String name) {
		try {
			this.cache.clear(name);
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> void remove(String name, Long id) {
		try {
			this.cache.remove(name, id);
		} finally {
			notifyAll();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized <T> T get(String name, Long id) {
		try {
			return (T) this.cache.get(name, id);
		} finally {
			notifyAll();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized <T> T get(String name, String sql) {
		return (T) cache.get(name, sql);
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized <T> List<T> get(Class<T> klass, String name, ICriteria<T> criteria, IAction<T> action, int size) {
		ILock lock = null;
		try {
			lock = AtomicAction.lock(name);
			if (lock == null) {
				return Arrays.asList();
			}
			return cache.get(name, criteria, action, size);
		} finally {
			AtomicAction.unlock(lock);
			notifyAll();
		}
	}

	@Override
	public ICache getCache() {
		return cache;
	}

	@Override
	public boolean isException() {
		try {
			return exception;
		} finally {
			exception = Boolean.FALSE;
		}
	}

	@Override
	public void setException(boolean exception) {
		this.exception = exception;
	}

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