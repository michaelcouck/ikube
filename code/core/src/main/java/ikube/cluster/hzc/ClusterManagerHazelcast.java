package ikube.cluster.hzc;

import ikube.IConstants;
import ikube.cluster.AClusterManager;
import ikube.cluster.jms.ClusterManagerJmsLock;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.service.IMonitorService;
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

/**
 * This action checks that the disk is not full, the one where the indexes are, if it is then this instance will close down.
 * 
 * @author Michael Couck
 * @since 15.07.12
 * @version 01.00
 */
public class ClusterManagerHazelcast extends AClusterManager {
	
	@Autowired
	private IMonitorService monitorService;

	public ClusterManagerHazelcast() {
		initialize();
	}

	public void initialize() {
		ip = UriUtilities.getIp();
		address = ip + "." + Hazelcast.getConfig().getPort();
		logger.info("Cluster manager : " + ip + ", " + address);
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
		try {
			Action action = getAction(actionName, indexName, indexableName);
			Server server = getServer();
			if (server.getAddress() == null) {
				server.setAddress(address);
			}
			server.getActions().add(action);
			logger.debug("Start working : {} ", action);
			Map<String, Server> servers = getServers();
			servers.put(address, server);
			return action;
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void stopWorking(Action action) {
		try {
			logger.debug("Stop working : {} ", action);

			action.setEndTime(new Timestamp(System.currentTimeMillis()));
			action.setDuration(action.getEndTime().getTime() - action.getStartTime().getTime());

			Server server = getServer();
			Iterator<Action> iterator = server.getActions().iterator();
			boolean removed = false;
			// We must iterate over the actions and remove the one by id because the
			// equals is modified by Hazelcast it seems
			while (iterator.hasNext()) {
				if (iterator.next().getId() == action.getId()) {
					iterator.remove();
					removed = true;
				}
			}
			if (!removed) {
				logger.warn("Didn't removed action : {} {} ", new Object[] { action, server.getActions() });
			}
			Map<String, Server> servers = getServers();
			servers.put(server.getAddress(), server);
			dataBase.merge(action);
		} finally {
			notifyAll();
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
		Map<String, Server> servers = getServers();
		Server server = (Server) servers.get(address);
		if (server == null) {
			server = new Server();
			logger.info("Server null, creating new one : " + server);
		}
		long time = System.currentTimeMillis();
		server.setIp(ip);
		server.setId(time);
		server.setAge(time);
		server.setAddress(address);
		Collection<IndexContext> collection = monitorService.getIndexContexts().values();
		List<IndexContext> indexContexts = new ArrayList<IndexContext>(collection);
		server.setIndexContexts(indexContexts);
		servers.put(address, server);
		return server;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendMessage(Serializable serializable) {
		// Don't need to do anything, the distributed map should be updated
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