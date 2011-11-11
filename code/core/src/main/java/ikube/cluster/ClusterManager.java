package ikube.cluster;

import ikube.IConstants;
import ikube.cluster.cache.ICache;
import ikube.model.Action;
import ikube.model.Server;
import ikube.service.IMonitorWebService;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.HashUtilities;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This class is responsible for the cluster synchronisation functionality.
 * 
 * @see IClusterManager
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ClusterManager implements IClusterManager, IConstants {

	/** The logger, doh. */
	protected static final Logger LOGGER = Logger.getLogger(ClusterManager.class);

	/** The ip of this server. */
	private transient String ip;
	/**
	 * The address of this server, this must be unique in the cluster. Typically this is the ip address added to the system time. The chance
	 * of a hash clash with any other server is in the billions, we will disregard this possibility on prudent grounds.
	 */
	private transient String address;
	protected transient ICache cache;
	/** This flag is set cluster wide to make exception for the rules. */
	private transient boolean exception;

	private Map<String, Server> servers;

	/**
	 * In the constructor we initialise the logger but most importantly the address of this server. Please see the comments.
	 * 
	 * @throws UnknownHostException
	 *             this can only be the InetAddress exception, which can never happen because we are looking for the localhost
	 */
	public ClusterManager() throws UnknownHostException {
		// We give each server a unique name because there can be several servers
		// started on the same machine. For example several Tomcats each with a war. In
		// this case the ip addresses will overlap. Ikube can also be started as stand alone
		// just in a Jvm(which is the same as a Tomcat essentially) also they will have the
		// same ip address
		this.ip = InetAddress.getLocalHost().getHostAddress();
		this.address = ip + "." + System.nanoTime();
		this.servers = new HashMap<String, Server>();
	}

	public void setCache(ICache cache) {
		this.cache = cache;
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
			LOGGER.debug("Setting action : " + indexName + ", " + indexableName + ", " + batchSize);
			action = new Action(0, null, indexableName, indexName, new Timestamp(System.currentTimeMillis()), Boolean.TRUE);
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
		// set(Server.class.getName(), server.getId(), server);
		servers.put(server.getAddress(), server);
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
			// ISearcherWebService.PUBLISHED_PATH
			int monitoringPort = getServicePort(IMonitorWebService.class, IMonitorWebService.PUBLISHED_PORT,
					IMonitorWebService.PUBLISHED_PATH, IMonitorWebService.NAMESPACE, IMonitorWebService.SERVICE, 0);
			int searcherPort = getServicePort(ISearcherWebService.class, ISearcherWebService.PUBLISHED_PORT,
					ISearcherWebService.PUBLISHED_PATH, ISearcherWebService.NAMESPACE, ISearcherWebService.SERVICE, 0);
			server.setMonitoringWebServicePort(monitoringPort);
			server.setSearchWebServicePort(searcherPort);

			try {
				java.net.URL searcherWebServiceUrl = new java.net.URL("http", ip, searcherPort, ISearcherWebService.PUBLISHED_PATH);
				server.setSearchWebServiceUrl(searcherWebServiceUrl.toString());
			} catch (MalformedURLException e) {
				LOGGER.error("Exception setting the search web service url : ", e);
			}

			cache.set(Server.class.getName(), server.getId(), server);
			LOGGER.debug("Published server : " + server);
			LOGGER.debug("Server from cache : " + cache.get(Server.class.getName(), server.getId()));
		}
		return server;
	}

	private int getServicePort(Class<?> klass, int port, String path, String nameSpace, String serviceName, int retryCount) {
		try {
			Object service = ServiceLocator.getService(klass, "http", ip, port, path, nameSpace, serviceName);
			if (service != null) {
				return port;
			}
		} catch (Exception e) {
			if (IConstants.MAX_RETRY_COUNTER < retryCount) {
				return getServicePort(klass, ++port, path, nameSpace, serviceName, ++retryCount);
			}
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized long startWorking(final String actionName, final String indexName, final String indexableName) {
		try {
			long startTime = System.currentTimeMillis();
			Server server = getServer();
			Action action = new Action();
			// Reset the id of this action
			action.setIdNumber(0);
			action.setActionName(actionName);
			action.setIndexName(indexName);
			action.setIndexableName(indexableName);
			// Reset the time in the action for each action
			action.setStartTime(new Timestamp(startTime));
			action.setWorking(Boolean.TRUE);
			action.setServerName(server.getAddress());
			// Publish the fact that this server is starting to work on an action
			server.setAction(action);
			servers.put(server.getAddress(), server);
			// set(Server.class.getName(), server.getId(), server);
			LOGGER.debug("Published action : " + getServer().getAction());
			return startTime;
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void stopWorking(final String actionName, final String indexName, final String indexableName) {
		try {
			Server server = getServer();
			Action action = server.getAction();
			if (action != null) {
				action.setWorking(Boolean.FALSE);
				action.setEndTime(new Timestamp(System.currentTimeMillis()));
				action.setDuration(action.getEndTime().getTime() - action.getStartTime().getTime());
			}
			server.setAction(null);
			servers.put(server.getAddress(), server);
			// set(Server.class.getName(), server.getId(), server);
		} catch (Exception e) {
			LOGGER.error("Exception stopping working : " + actionName + ", " + indexName + ", " + indexableName, e);
			new Thread(new Runnable() {
				public void run() {
					stopWorkingRetry(actionName, indexName, indexableName, 0);
				}
			}).start();
		} finally {
			notifyAll();
		}
	}

	private void stopWorkingRetry(final String actionName, final String indexName, final String indexableName, int retryCount) {
		try {
			Server server = getServer();
			Action action = server.getAction();
			if (action != null) {
				action.setWorking(Boolean.FALSE);
				action.setEndTime(new Timestamp(System.currentTimeMillis()));
				action.setDuration(action.getEndTime().getTime() - action.getStartTime().getTime());
			}
			server.setAction(null);
			servers.put(server.getAddress(), server);
			// set(Server.class.getName(), server.getId(), server);
		} catch (Exception e) {
			LOGGER.error("Exception re-trying to stop working : " + actionName + ", " + indexName + ", " + indexableName, e);
			if (retryCount < MAX_RETRY_COUNTER) {
				stopWorkingRetry(actionName, indexName, indexableName, ++retryCount);
			}
		}
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

	@Override
	public boolean lock(final String name) {
		return cache.lock(name);
	}

	@Override
	public boolean unlock(String name) {
		return cache.unlock(name);
	}

	public void setAddress(String address) {
		this.address = address;
	}

}