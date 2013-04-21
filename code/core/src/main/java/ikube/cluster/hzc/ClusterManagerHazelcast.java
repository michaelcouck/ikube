package ikube.cluster.hzc;

import ikube.IConstants;
import ikube.cluster.AClusterManager;
import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ILock;
import com.hazelcast.core.MessageListener;
import com.hazelcast.nio.HazelcastSerializationException;

/**
 * @see IClusterManager
 * @author Michael Couck
 * @since 15.07.12
 * @version 01.00
 */
public final class ClusterManagerHazelcast extends AClusterManager {

	@Autowired
	private IMonitorService monitorService;

	private transient Server server;

	public void setListeners(final List<MessageListener<Object>> listeners) {
		ip = UriUtilities.getIp();
		address = ip + "-" + Hazelcast.getCluster().getLocalMember().getInetSocketAddress().getPort();
		logger.info("Cluster manager : " + ip + ", " + address);
		Hazelcast.getConfig().getNetworkConfig().getInterfaces().setInterfaces(Arrays.asList(ip));
		for (final MessageListener<Object> listener : listeners) {
			if (listener == null) {
				continue;
			}
			Hazelcast.getTopic(IConstants.TOPIC).addMessageListener(listener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean lock(final String name) {
		try {
			ILock lock = Hazelcast.getLock(name);
			boolean gotLock = false;
			try {
				gotLock = lock.tryLock(250, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				logger.error("Exception trying for the lock : ", e);
			}
			return gotLock;
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean unlock(final String name) {
		try {
			ILock lock = Hazelcast.getLock(name);
			lock.unlock();
			return true;
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean anyWorking() {
		Map<String, Server> servers = getServers();
		for (Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			if (server.isWorking()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean anyWorking(final String indexName) {
		Map<String, Server> servers = getServers();
		for (Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			if (!server.isWorking() || server.getActions() == null) {
				continue;
			}
			for (Action action : server.getActions()) {
				if (indexName.equals(action.getIndexName())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Action startWorking(final String actionName, final String indexName, final String indexableName) {
		Hazelcast.getTransaction().begin();
		Action action = null;
		Server server = getServer();
		try {
			action = getAction(actionName, indexName, indexableName);
			server.getActions().add(action);
			server = (Server) SerializationUtilities.clone(server);
			Hazelcast.getMap(IConstants.IKUBE).put(server.getAddress(), server);
			Hazelcast.getTransaction().commit();
		} catch (Exception e) {
			logger.error("Exception starting action : " + actionName + ", " + indexName + ", " + indexableName, e);
			Hazelcast.getTransaction().rollback();
			stopWorking(action);
			action = null;
		} finally {
			notifyAll();
		}
		return action;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void stopWorking(final Action action) {
		try {
			stopWorking(action, 0);
		} finally {
			notifyAll();
		}
	}

	private synchronized void stopWorking(final Action action, final int retry) {
		if (action == null) {
			logger.warn("Action null : " + action);
			return;
		}
		boolean removedAndComitted = false;
		Hazelcast.getTransaction().begin();
		Server server = getServer();
		Action toRemoveAction = null;
		try {
			action.setEndTime(new Timestamp(System.currentTimeMillis()));
			action.setDuration(action.getEndTime().getTime() - action.getStartTime().getTime());
			Iterator<Action> iterator = server.getActions().iterator();
			// We must iterate over the actions and remove the one by id because the
			// equals is modified by Hazelcast it seems
			boolean removedFromServerActions = false;
			while (iterator.hasNext()) {
				toRemoveAction = iterator.next();
				if (toRemoveAction.getId() == action.getId()) {
					iterator.remove();
					removedFromServerActions = true;
					break;
				}
			}
			if (removedFromServerActions) {
				server = (Server) SerializationUtilities.clone(server);
				Hazelcast.getMap(IConstants.IKUBE).put(server.getAddress(), server);
			}
			// Commit the grid because the database is not as important as the cluster
			Hazelcast.getTransaction().commit();
			removedAndComitted = true;
			if (dataBase.find(Action.class, action.getId()) != null) {
				dataBase.merge(action);
			}
		} catch (ConcurrentModificationException e) {
			logger.warn("Concurrent error, will retry : " + e.getMessage());
			Hazelcast.getTransaction().rollback();
		} catch (HazelcastSerializationException e) {
			logger.warn("Concurrent error, will retry : " + e.getMessage());
			Hazelcast.getTransaction().rollback();
		} catch (Exception e) {
			logger.error("Exception stopping action : " + action, e);
			logger.error("Removed action not comitted : " + toRemoveAction);
			Hazelcast.getTransaction().rollback();
		} finally {
			if (!removedAndComitted) {
				ThreadUtilities.sleep(1000);
				if (server != null && server.getActions() != null && server.getActions().size() > 0) {
					if (retry >= IConstants.MAX_RETRY_CLUSTER_REMOVE) {
						logger.info("Retried to remove the action, failed : " + retry + ", action : " + action + ", actions : "
								+ server.getActions());
						return;
					} else {
						logger.debug("Retrying to remove the action : " + retry + ", " + server.getIp() + ", " + action + ", "
								+ server.getActions());
						stopWorking(action, retry + 1);
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Server> getServers() {
		return Hazelcast.getMap(IConstants.IKUBE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Server getServer() {
		try {
			Server server = (Server) Hazelcast.getMap(IConstants.IKUBE).get(address);
			if (server != null) {
				// We set the instance server to the one in
				// Hazelcast so if Hazelcast drops the server we have a
				// copy to replace it
				this.server = server;
			} else {
				// First check if the instance server is still active
				if (this.server != null) {
					server = this.server;
					logger.warn("Server dropped from Hazelcast! Re-inserting into the grid... " + server);
					try {
						Hazelcast.getTransaction().begin();
						server = (Server) SerializationUtilities.clone(server);
						Hazelcast.getMap(IConstants.IKUBE).put(server.getAddress(), server);
						Hazelcast.getTransaction().commit();
					} catch (Exception e) {
						logger.error("Exception re-injecting the server in the grid : " + server.getAddress(), e);
						Hazelcast.getTransaction().rollback();
					}
				} else {
					server = new Server();
					server.setIp(ip);
					server.setAddress(address);
					server.setId(System.currentTimeMillis());
					logger.info("Server null, creating new one : " + server);
				}
			}
			server.setAge(System.currentTimeMillis());

			Collection<IndexContext> collection = monitorService.getIndexContexts().values();
			List<IndexContext> indexContexts = new ArrayList<IndexContext>(collection);
			server.setIndexContexts(indexContexts);

			return server;
		} catch (Exception e) {
			logger.error("Exception getting the server : ", e);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendMessage(final Serializable serializable) {
		Hazelcast.getTopic(IConstants.TOPIC).publish(serializable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getObject(final Object key) {
		return (T) Hazelcast.getMap(IConstants.IKUBE).get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putObject(final Object key, final Object value) {
		Hazelcast.getMap(IConstants.IKUBE).put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(final Object key) {
		Hazelcast.getMap(IConstants.IKUBE).remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
		Hazelcast.shutdownAll();
	}

}