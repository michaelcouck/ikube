package ikube.cluster.hzc;

import ikube.IConstants;
import ikube.cluster.AClusterManager;
import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.toolkit.UriUtilities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.MessageListener;

/**
 * @see IClusterManager
 * @author Michael Couck
 * @since 15.07.12
 * @version 01.00
 */
public final class ClusterManagerHazelcast extends AClusterManager {

	/** The instance of this server. */
	private Server server;
	@Autowired
	private IMonitorService monitorService;
	private HazelcastInstance hazelcastInstance;

	public void setListeners(final List<MessageListener<Object>> listeners) {
		ip = UriUtilities.getIp();
		hazelcastInstance = Hazelcast.getHazelcastInstanceByName(IConstants.IKUBE);
		if (hazelcastInstance == null) {
			hazelcastInstance = Hazelcast.newHazelcastInstance();
		}
		address = ip + "-" + hazelcastInstance.getCluster().getLocalMember().getInetSocketAddress().getPort();
		hazelcastInstance.getConfig().getNetworkConfig().getInterfaces().setInterfaces(Arrays.asList(ip));
		if (listeners != null) {
			for (final MessageListener<Object> listener : listeners) {
				if (listener == null) {
					continue;
				}
				hazelcastInstance.getTopic(IConstants.TOPIC).addMessageListener(listener);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean lock(final String name) {
		try {
			ILock lock = hazelcastInstance.getLock(name);
			boolean gotLock = false;
			try {
				gotLock = lock.tryLock(250, TimeUnit.MILLISECONDS);
				logger.debug("Got lock : " + gotLock + ", thread : " + Thread.currentThread().hashCode());
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
			ILock lock = hazelcastInstance.getLock(name);
			if (lock.isLocked()) {
				if (lock.isLockedByCurrentThread()) {
					logger.debug("Unlocking : " + Thread.currentThread().hashCode());
					lock.unlock();
				}
			}
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
		for (final Map.Entry<String, Server> mapEntry : servers.entrySet()) {
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
		for (final Map.Entry<String, Server> mapEntry : servers.entrySet()) {
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
		Action action = null;
		try {
			Server server = getServer();
			action = getAction(actionName, indexName, indexableName);
			server.getActions().add(action);
			hazelcastInstance.getMap(IConstants.IKUBE).put(server.getAddress(), server);
		} catch (Exception e) {
			logger.error("Exception starting action : " + actionName + ", " + indexName + ", " + indexableName, e);
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
			// Persist the action with the end date
			action.setEndTime(new Timestamp(System.currentTimeMillis()));
			action.setDuration(action.getEndTime().getTime() - action.getStartTime().getTime());
			dataBase.merge(action);
			Server server = getServer();
			List<Action> actions = server.getActions();
			// Remove the action from the grid
			Iterator<Action> actionIterator = actions.iterator();
			while (actionIterator.hasNext()) {
				Action gridAction = actionIterator.next();
				if (gridAction.getId() == action.getId()) {
					actionIterator.remove();
					logger.debug("Removed grid action : " + gridAction.getId() + ", " + actions.size());
				}
			}
			hazelcastInstance.getMap(IConstants.IKUBE).put(server.getAddress(), server);
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Server> getServers() {
		return hazelcastInstance.getMap(IConstants.IKUBE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Server getServer() {
		if (server == null) {
			server = new Server();
			server.setIp(ip);
			server.setAddress(address);
			server.setAge(System.currentTimeMillis());
			logger.debug("Server null, creating new one : " + server);

			Collection<IndexContext> collection = monitorService.getIndexContexts().values();
			List<IndexContext> indexContexts = new ArrayList<IndexContext>(collection);
			server.setIndexContexts(indexContexts);

			dataBase.persist(server);
		}
		return server;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendMessage(final Serializable serializable) {
		hazelcastInstance.getTopic(IConstants.TOPIC).publish(serializable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(final Object key) {
		hazelcastInstance.getMap(IConstants.IKUBE).remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
		Hazelcast.shutdownAll();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(final Object key) {
		return hazelcastInstance.getMap(IConstants.IKUBE).get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(final Object key, final Serializable value) {
		hazelcastInstance.getMap(IConstants.IKUBE).put(key, value);
	}

}