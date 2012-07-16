package ikube.cluster.jgp;

import ikube.IConstants;
import ikube.cluster.AClusterManager;
import ikube.cluster.jms.ClusterManagerJmsLock;
import ikube.model.Action;
import ikube.model.Server;
import ikube.toolkit.UriUtilities;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.jgroups.JChannel;
import org.jgroups.blocks.locking.LockService;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

/**
 * This action checks that the disk is not full, the one where the indexes are, if it is then this instance will close down.
 * 
 * @author Michael Couck
 * @since 15.07.12
 * @version 01.00
 */
public class ClusterManagerJgroups extends AClusterManager {
	
	private JChannel channel;
	private LockService lockService;

	public void initialize() throws Exception {
		InputStream inputStream = getClass().getResourceAsStream("/udp.xml");
		channel = new JChannel(inputStream);
		channel.connect(IConstants.IKUBE);
		lockService = new LockService(channel);
		ip = UriUtilities.getIp();
		address = ip;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean lock(String name) {
		try {
			Lock lock =  lockService.getLock(IConstants.IKUBE);
			try {
				if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {
					logger.info("Got lock : " + Thread.currentThread().hashCode());
					return true;
				}
			} catch (InterruptedException e) {
				logger.error(null, e);
			}
			return false;
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
			Lock lock =  lockService.getLock(IConstants.IKUBE);
			try {
				if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {
					logger.info("Had lock : " + Thread.currentThread().hashCode());
					lock.unlock();
					return true;
				}
			} catch (InterruptedException e) {
				logger.error(null, e);
			}
			return false;
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
			server.getActions().add(action);
			logger.debug("Start working : {} ", action);
			IMap<String, Server> servers = Hazelcast.getMap(IConstants.SERVERS);
			servers.put(server.getAddress(), server);
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
			action.setEndTime(new Timestamp(System.currentTimeMillis()));
			action.setDuration(action.getEndTime().getTime() - action.getStartTime().getTime());

			Server server = getServer();
			logger.debug("Stop working : {} ", action);
			Iterator<Action> iterator = server.getActions().iterator();
			boolean removed = false;
			// We must iterate oveeer the actions and remove the one by id because the
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
			dataBase.merge(action);
			Map<String, Server> servers = getServers();
			servers.put(server.getAddress(), server);
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
	public Server getServer() {
		Map<String, Server> servers = getServers();
		Server server = (Server) servers.get(address);
		if (server != null) {
			return server;
		}
		server = new Server();
		servers.put(address, server);
		logger.info("Server null, creating new one : " + server);
		long time = System.currentTimeMillis();
		server.setIp(ip);
		server.setId(time);
		server.setAge(time);
		server.setAddress(address);
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