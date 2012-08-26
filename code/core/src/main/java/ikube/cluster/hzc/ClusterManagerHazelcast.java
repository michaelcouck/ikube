package ikube.cluster.hzc;

import ikube.IConstants;
import ikube.cluster.AClusterManager;
import ikube.cluster.jms.ClusterManagerJmsLock;
import ikube.listener.Event;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.service.IMonitorService;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ILock;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.core.Transaction;

/**
 * @author Michael Couck
 * @since 15.07.12
 * @version 01.00
 */
public class ClusterManagerHazelcast extends AClusterManager {

	@Autowired
	private IMonitorService monitorService;

	public void initialize() {
		ip = UriUtilities.getIp();
		address = ip + "." + Hazelcast.getConfig().getPort();
		logger.info("Cluster manager : " + ip + ", " + address);
		Hazelcast.getTopic(IConstants.TOPIC).addMessageListener(new MessageListener<Object>() {
			@Override
			public void onMessage(Message<Object> message) {
				// If this is a stop working message then find the future in the
				// thread utilities and kill it
				Object source = message.getSource();
				Object object = message.getMessageObject();
				logger.info("Got message : " + source + ", " + object);
				if (object != null && Event.class.isAssignableFrom(object.getClass())) {
					Event event = (Event) object;
					if (event.getObject() != null && String.class.isAssignableFrom(event.getObject().getClass())) {
						ThreadUtilities.destroy((String) event.getObject());
					}
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean lock(String name) {
		try {
			ILock lock = Hazelcast.getLock(name);
			boolean gotLock = false;
			try {
				gotLock = lock.tryLock(500, TimeUnit.MILLISECONDS);
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
	public synchronized boolean unlock(String name) {
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
	public boolean anyWorking(String indexName) {
		Map<String, Server> servers = getServers();
		for (Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			if (server.isWorking()) {
				if (server.getActions() != null) {
					for (Action action : server.getActions()) {
						if (indexName.equals(action.getIndexName())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Action startWorking(String actionName, String indexName, String indexableName) {
		Transaction transaction = Hazelcast.getTransaction();
		transaction.begin();
		Action action = null;
		try {
			// TODO Do we need to lock the cluster at every read?
			action = getAction(actionName, indexName, indexableName);
			Server server = getServer();
			if (server.getAddress() == null) {
				server.setAddress(address);
			}
			server.getActions().add(action);
			Hazelcast.getMap(IConstants.SERVERS).put(server.getAddress(), server);
			transaction.commit();
		} catch (Exception e) {
			logger.error("Exception starting action : " + actionName + ", " + indexName + ", " + indexableName, e);
			transaction.rollback();
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
			stopWorking(action, 10);
		} finally {
			notifyAll();
		}
	}

	private void stopWorking(final Action action, final int retry) {
		if (action == null) {
			logger.warn("Action null : " + action);
			return;
		}
		if (retry > IConstants.MAX_RETRY_CLUSTER_REMOVE) {
			logger.warn("Retried to remove the action, failed : " + retry);
			return;
		}
		Transaction transaction = Hazelcast.getTransaction();
		transaction.begin();
		boolean removedAndComitted = false;
		Server server = getServer();
		try {
			logger.warn("Stop working : {} ", action);
			action.setEndTime(new Timestamp(System.currentTimeMillis()));
			action.setDuration(action.getEndTime().getTime() - action.getStartTime().getTime());
			Iterator<Action> iterator = server.getActions().iterator();
			// We must iterate over the actions and remove the one by id because the
			// equals is modified by Hazelcast it seems
			boolean removedFromServerActions = false;
			while (iterator.hasNext()) {
				if (iterator.next().getId() == action.getId()) {
					iterator.remove();
					removedFromServerActions = true;
				}
			}
			Hazelcast.getMap(IConstants.SERVERS).put(server.getAddress(), server);
			// Commit the grid because the database is not as important as the cluster
			transaction.commit();
			dataBase.merge(action);
			removedAndComitted = removedFromServerActions;
		} catch (Exception e) {
			logger.error("Exception stopping action : " + action, e);
			transaction.rollback();
		} finally {
			if (!removedAndComitted) {
				logger.warn("Retrying to remove the action : " + retry + ", " + action);
				stopWorking(action, retry + 1);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Server> getServers() {
		return Hazelcast.getMap(IConstants.SERVERS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Server getServer() {
		try {
			Server server = (Server) Hazelcast.getMap(IConstants.SERVERS).get(address);
			if (server == null) {
				server = new Server();
				server.setIp(ip);
				server.setAddress(address);
				logger.info("Server null, creating new one : " + server);
			}
			long time = System.currentTimeMillis();
			server.setId(time);
			server.setAge(time);

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
	public void sendMessage(Serializable serializable) {
		Hazelcast.getTopic(IConstants.TOPIC).publish(serializable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, ClusterManagerJmsLock> getLocks() {
		// Don't need to remove locks, should be done automatically
		return Collections.EMPTY_MAP;
	}

}