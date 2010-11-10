package ikube.toolkit;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.Batch;
import ikube.model.IndexContext;
import ikube.model.Lock;
import ikube.model.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class ClusterManager {

	private static Logger LOGGER = Logger.getLogger(ClusterManager.class);
	private static IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);

	/**
	 * This method checks to see if this server is working on this index.
	 * 
	 * @param indexContext
	 *            the index context that this check is for
	 * @return whether this server is working on this index
	 */
	public static Boolean isWorking(IndexContext indexContext) {
		Lock lock = dataBase.lock(Server.class);
		try {
			Server server = getServer(indexContext);
			if (server.isWorking()) {
				LOGGER.info("Already indexing this configuration : " + indexContext.getIndexName() + ", " + indexContext.getServerName());
			}
			return server.isWorking();
		} finally {
			dataBase.release(lock);
		}
	}

	/**
	 * Simply sets the working flag for this server for this index.
	 * 
	 * @param working
	 *            the working object to set
	 * @param isWorking
	 *            if this server is working on this index
	 */
	public static void setWorking(IndexContext indexContext, String actionName, Boolean isWorking) {
		setWorking(indexContext, actionName, isWorking, Boolean.TRUE);
	}

	private static void setWorking(IndexContext indexContext, String actionName, Boolean isWorking, boolean mustLock) {
		Lock lock = null;
		try {
			if (mustLock) {
				lock = dataBase.lock(Server.class);
			}
			Server server = getServer(indexContext);
			server.setActionName(actionName);
			server.setWorking(isWorking);
			if (isWorking) {
				server.setStart(new Timestamp(System.currentTimeMillis()));
			} else {
				server.setStart(null);
			}
			dataBase.merge(server);
			LOGGER.info("Set working : " + server);
		} finally {
			if (mustLock) {
				dataBase.release(lock);
			}
		}
		// Thread.dumpStack();
	}

	/**
	 * Returns whether any servers are working on any action other than this action.
	 * 
	 * @param actionName
	 *            the action name to check if any other servers are doing anything else
	 * @param indexContext
	 *            the index context for the index
	 * @return whether there are any servers working on any other action than the one specified in the parameter list
	 */
	public static boolean anyWorking(String actionName, IndexContext indexContext) {
		return anyWorking(actionName, indexContext, Boolean.TRUE);
	}

	private static boolean anyWorking(String actionName, IndexContext indexContext, boolean mustLock) {
		Lock lock = null;
		boolean anyWorking = Boolean.FALSE;
		try {
			if (mustLock) {
				lock = dataBase.lock(Server.class);
			}
			List<Server> servers = getServers(indexContext);
			LOGGER.info("Any working : Workings : " + servers + ", " + Thread.currentThread().hashCode());
			if (servers == null || servers.size() == 0) {
				return Boolean.FALSE;
			}
			for (Server other : servers) {
				// If there are any other servers doing an action other than this working action
				if (!other.isWorking()) {
					continue;
				}
				String otherActionName = other.getActionName();
				if (!actionName.equals(otherActionName)) {
					anyWorking = Boolean.TRUE;
					break;
				}
			}
		} finally {
			if (mustLock) {
				dataBase.release(lock);
			}
		}
		return anyWorking;
	}

	/**
	 * Checks to see if there are any other servers working on this index, with this action.
	 * 
	 * @param actionName
	 *            the name of the action we want to see if there are any servers working on
	 * @param indexContext
	 *            the index context with the index name and the server name
	 * @return whether there are any servers working on this index with this action, other than ourselves of course
	 */
	public static boolean areWorking(String actionName, IndexContext indexContext) {
		Lock lock = dataBase.lock(Server.class);
		boolean areWorking = Boolean.FALSE;
		try {
			Server server = getServer(indexContext);
			List<Server> servers = getServers(indexContext);
			if (servers == null || servers.size() == 0) {
				return areWorking;
			}
			for (Server other : servers) {
				if (!other.isWorking()) {
					continue;
				}
				if (other.getId() == server.getId()) {
					continue;
				}
				String otherActionName = other.getActionName();
				if (actionName.equals(otherActionName)) {
					areWorking = Boolean.TRUE;
					break;
				}
			}
			LOGGER.info("Are workings : " + areWorking + ", " + servers + ", " + Thread.currentThread().hashCode());
		} finally {
			dataBase.release(lock);
		}
		return areWorking;
	}

	public static long getLastWorkingTime(String actionName, IndexContext indexContext) {
		Lock lock = dataBase.lock(Server.class);
		long time = System.currentTimeMillis();
		try {
			ClusterManager.setWorking(indexContext, actionName, Boolean.TRUE, Boolean.FALSE);
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put(IConstants.INDEX_NAME, indexContext.getIndexName());
			List<Server> servers = dataBase.find(Server.class, parameters, 0, Integer.MAX_VALUE);
			for (Server server : servers) {
				if (!server.isWorking()) {
					continue;
				}
				// This means that there is a server working on an action
				// other than this action, so we return -1 for the time so
				// the indexer will not start
				if (!actionName.equals(server.getActionName())) {
					return -1;
				}
				Timestamp start = server.getStart();
				// We want the start time of the first server, so if the start time
				// of this server is lower than the existing start time then take that
				if (start.getTime() < time) {
					LOGGER.info("Taking start time of server : " + server);
					time = start.getTime();
				}
			}
			LOGGER.info("Workings : " + servers + ", " + Thread.currentThread().hashCode() + ", " + time);
		} finally {
			dataBase.release(lock);
		}
		return time;
	}

	public static boolean resetWorkings(String actionName, IndexContext indexContext) {
		Lock lock = dataBase.lock(Server.class);
		try {
			if (ClusterManager.anyWorking(actionName, indexContext, Boolean.FALSE)) {
				LOGGER.info("Reset : Other servers working : ");
				return Boolean.FALSE;
			}
			ClusterManager.setWorking(indexContext, actionName, Boolean.TRUE, Boolean.FALSE);
			Server server = ClusterManager.getServer(indexContext);
			List<Server> servers = ClusterManager.getServers(indexContext);
			boolean othersWorking = Boolean.FALSE;
			LOGGER.info("Reseting workings : " + servers + ", " + Thread.currentThread().hashCode());
			for (Server other : servers) {
				if (other.equals(server)) {
					continue;
				}
				if (other.isWorking()) {
					othersWorking = Boolean.TRUE;
					break;
				}
			}
			LOGGER.info("Others working : " + othersWorking + ", " + servers);
			if (othersWorking) {
				return Boolean.FALSE;
			}
			// If there are no servers working except for this one then reset all the flags
			// for this index context for all servers
			for (Server other : servers) {
				other.setStart(null);
				other.setWorking(Boolean.FALSE);
			}
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put(IConstants.INDEX_NAME, indexContext.getIndexName());
			Batch batch = dataBase.find(Batch.class, parameters, Boolean.TRUE);
			if (batch == null) {
				batch = new Batch();
			}
			batch.setNextRowNumber(0);
			dataBase.merge(batch);
			return Boolean.TRUE;
		} finally {
			ClusterManager.setWorking(indexContext, null, Boolean.FALSE, Boolean.FALSE);
			dataBase.release(lock);
		}
	}

	public static int getNextBatchNumber(IndexContext indexContext) {
		Lock lock = dataBase.lock(Server.class);
		int nextBatchNumber = 0;
		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put(IConstants.INDEX_NAME, indexContext.getIndexName());
			// Get the highest batch number from the working servers
			Batch batch = dataBase.find(Batch.class, parameters, Boolean.TRUE);
			if (batch == null) {
				batch = new Batch();
				batch.setIndexName(indexContext.getIndexName());
			}
			// Get the next row number
			nextBatchNumber = batch.getNextRowNumber();
			// Increment the batch number by the context batch size
			batch.setNextRowNumber(batch.getNextRowNumber() + (int) indexContext.getBatchSize());
			dataBase.merge(batch);
		} finally {
			dataBase.release(lock);
		}
		return nextBatchNumber;
	}

	/**
	 * Gets the working flag for this server and index.
	 * 
	 * @param indexContext
	 *            the index context for the index
	 * @return the working flag for the server and index
	 */
	protected static Server getServer(IndexContext indexContext) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.INDEX_NAME, indexContext.getIndexName());
		parameters.put(IConstants.SERVER_NAME, indexContext.getServerName());
		Server server = dataBase.find(Server.class, parameters, Boolean.TRUE);
		if (server == null) {
			server = new Server();
			server.setIndexName(indexContext.getIndexName());
			server.setServerName(indexContext.getServerName());
			try {
				server.setServerIpAddress(InetAddress.getLocalHost().getHostAddress());
			} catch (UnknownHostException e) {
				LOGGER.error("Who am I?", e);
			}
			dataBase.persist(server);
		}
		return server;
	}

	/**
	 * Returns all the working flags for all the servers for this index configuration.
	 * 
	 * @param indexContext
	 *            the index context to access for all servers
	 * @return the list of working flags for all servers for this index
	 */
	public static List<Server> getServers(IndexContext indexContext) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.INDEX_NAME, indexContext.getIndexName());
		List<Server> servers = dataBase.find(Server.class, parameters, 0, Integer.MAX_VALUE);
		return servers;
	}

}