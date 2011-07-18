package ikube.cluster;

import ikube.IConstants;
import ikube.cluster.cache.ICache;
import ikube.cluster.cache.ICache.IAction;
import ikube.cluster.cache.ICache.ICriteria;
import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.model.Action;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;

/**
 * This class is responsible for the cluster synchronization functionality.
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
		ITopic<Server> topic = Hazelcast.getTopic(IConstants.SHUTDOWN_TOPIC);
		topic.addMessageListener(new MessageListener<Server>() {
			@Override
			public void onMessage(final Server other) {
				if (other == null) {
					return;
				}
				LOGGER.info("Got shutdown message : " + other);
				Server server = ApplicationContextManager.getBean(IClusterManager.class).getServer();
				if (other.getAddress().equals(server.getAddress())) {
					// We don't shutdown our selves of course
					return;
				}
				long delay = 1000;
				ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
				executorService.schedule(new Runnable() {
					public void run() {
						LOGGER.warn("Shutting down Ikube server : " + other);
						ListenerManager.removeListeners();
						ApplicationContextManager.closeApplicationContext();
						Hazelcast.shutdownAll();
						System.exit(0);
					}
				}, delay, TimeUnit.MILLISECONDS);
				executorService.shutdown();
			}
		});
	}

	/**
	 * This method adds a listener to the cluster topic to make exception when evaluating the rules.
	 */
	public static void addClusterExceptionListener() {
		// Add the listener for general cluster directives like forcing an index to start
		LOGGER.info("Adding shutdown listener : ");
		ITopic<Boolean> topic = Hazelcast.getTopic(IConstants.EXCEPTION_TOPIC);
		topic.addMessageListener(new MessageListener<Boolean>() {
			@Override
			public void onMessage(Boolean command) {
				LOGGER.info("Got exception message : " + command);
				ApplicationContextManager.getBean(IClusterManager.class).setException(command);
			}
		});
	}

	/** The ip of this server. */
	private transient String ip;
	/**
	 * The address of this server, this must be unique in the cluster. Typically this is the ip address added to the system time. The chance
	 * of a hash clash with any other server is in the billions, we will disregard this possibility on prudent grounds.
	 */
	private transient String address;
	/** The cluster wide cache. */
	protected transient ICache cache;
	/** This flag is set cluster wide to make exception for the rules. */
	private transient boolean exception;

	/**
	 * This listener will respond to clean events and it will remove servers that have not checked in, i.e. their sell by date is expired.
	 */
	private IListener cleanerListener = new IListener() {
		@Override
		public void handleNotification(Event event) {
			if (!event.getType().equals(Event.CLEAN)) {
				return;
			}
			Server server = getServer();
			// Remove all servers that are past the max age
			List<Server> servers = getServers();
			for (Server remoteServer : servers) {
				if (remoteServer.getAddress().equals(server.getAddress())) {
					continue;
				}
				if (System.currentTimeMillis() - remoteServer.getAge() > MAX_AGE) {
					LOGGER.info("Removing server : " + remoteServer + ", " + (System.currentTimeMillis() - remoteServer.getAge() > MAX_AGE));
					remove(Server.class.getName(), remoteServer.getId());
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
			// Set our own server age
			Server server = getServer();
			server.setAge(System.currentTimeMillis());
			set(Server.class.getName(), server.getId(), server);
		}
	};

	/**
	 * In the constructor we initialize the logger but most importantly the address of this server. Please see the comments.
	 * 
	 * @throws Exception
	 *             this can only be the InetAddress exception, which can never happen because we are looking for the localhost
	 */
	public ClusterManager(ICache cache) throws Exception {
		this.cache = cache;
		// We give each server a unique name because there can be several servers
		// started on the same machine. For example several Tomcats each with a war. In
		// this case the ip addresses will overlap. Ikube can also be started as stand alone
		// just in a Jvm(which is the same as a Tomcat essentially) also they will have the
		// same ip address
		this.ip = InetAddress.getLocalHost().getHostAddress();
		this.address = ip + "." + System.nanoTime();
		Server server = getServer();
		LOGGER.info("This server started : " + server);
		// This listener will iterate over the servers and remove any that have expired
		// and will also register this server as still alive in the cluster
		ListenerManager.addListener(aliveListener);
		ListenerManager.addListener(cleanerListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean anyWorking() {
		Boolean anyWorking = Boolean.FALSE;
		Server server = getServer();
		List<Server> servers = getServers();
		for (Server other : servers) {
			if (other.getAddress().equals(server.getAddress())) {
				continue;
			}
			if (other.getWorking()) {
				anyWorking = Boolean.TRUE;
				break;
			}
		}
		return anyWorking;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean anyWorking(final String indexName) {
		Boolean anyWorking = Boolean.FALSE;
		List<Server> servers = getServers();
		outer: for (Server server : servers) {
			if (server.getWorking()) {
				if (server.getAction() != null && server.getAction().getIndexName().equals(indexName)) {
					anyWorking = Boolean.TRUE;
					break outer;
				}
			}
		}
		return anyWorking;
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
	public long getIdNumber(final String indexName, final String indexableName, final long batchSize, final long minId) {
		long idNumber = 0;
		// We find the action for this server
		Server server = getServer();
		Action action = server.getAction();
		if (action == null) {
			LOGGER.warn("Setting action : " + indexName + ", " + indexableName + ", " + batchSize);
			action = new Action(0, null, indexableName, indexName, System.currentTimeMillis(), Boolean.TRUE);
			server.setAction(action);
		}
		idNumber = action.getIdNumber();
		if (idNumber < minId) {
			idNumber = minId;
		}
		long nextIdNumber = idNumber + batchSize;
		// Set the next row number to the current + the batch size
		action.setIdNumber(nextIdNumber);
		// Publish the server to the cluster
		set(Server.class.getName(), server.getId(), server);
		return idNumber;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Server> getServers() {
		return cache.get(Server.class.getName(), null, null, Integer.MAX_VALUE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Server getServer() {
		Server server = cache.get(Server.class.getName(), HashUtilities.hash(address));
		if (server == null) {
			server = new Server();
			server.setIp(ip);
			server.setAddress(address);
			server.setId(HashUtilities.hash(address));
			server.setAge(System.currentTimeMillis());
			cache.set(Server.class.getName(), server.getId(), server);
			LOGGER.info("Published server : " + server);
			LOGGER.info("Server from cache : " + cache.get(Server.class.getName(), server.getId()));
		}
		return server;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized long setWorking(final String actionName, final String indexName, final String indexableName, final boolean isWorking) {
		try {
			Server server = getServer();
			server.setAction(new Action());
			// Reset the id of this action
			server.getAction().setIdNumber(0);
			server.getAction().setActionName(actionName);
			server.getAction().setIndexableName(indexableName);
			server.getAction().setIndexName(indexName);
			// Reset the time in the action for each action
			server.getAction().setStartTime(System.currentTimeMillis());
			server.getAction().setWorking(isWorking);
			// Publish the fact that this server is starting to work on an action
			set(Server.class.getName(), server.getId(), server);
			return server.getAction().getStartTime();
		} finally {
			notifyAll();
		}
	}

	@Override
	public <T> void set(String name, Long id, T object) {
		cache.set(name, id, object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> int size(String name) {
		return this.cache.size(name);
	}

	@Override
	public <T> void clear(String name) {
		this.cache.clear(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void remove(String name, Long id) {
		this.cache.remove(name, id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String name, Long id) {
		return (T) this.cache.get(name, id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String name, String sql) {
		return (T) cache.get(name, sql);
	}

	@Override
	public <T> List<T> get(final Class<T> klass, final String name, final ICriteria<T> criteria, final IAction<T> action, final int size) {
		return cache.get(name, criteria, action, size);
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

	public void setAddress(String address) {
		this.address = address;
	}

}