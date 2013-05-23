package ikube.cluster.hzc;

import ikube.IConstants;
import ikube.cluster.AClusterManager;
import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ILock;
import com.hazelcast.core.MessageListener;

/**
 * @see IClusterManager
 * @author Michael Couck
 * @since 15.07.12
 * @version 01.00
 */
public final class ClusterManagerHazelcast extends AClusterManager {

	@Value("${max.retry}")
	private int maxRetry;
	@Autowired
	private IMonitorService monitorService;

	public void setListeners(final List<MessageListener<Object>> listeners) {
		ip = UriUtilities.getIp();
		address = ip + "-" + Hazelcast.getCluster().getLocalMember().getInetSocketAddress().getPort();
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
			put(server.getAddress(), server);
		} catch (Exception e) {
			logger.error("Exception starting action : " + actionName + ", " + indexName + ", " + indexableName, e);
		} finally {
			notifyAll();
		}
		return action;
	}

	public interface IRetryCaller extends Runnable {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void stopWorking(final Action action) {
		try {
			class RetryCaller implements IRetryCaller {
				@Override
				public void run() {
					logger.debug("Retry caller : ");
					int maxRetry = ClusterManagerHazelcast.this.maxRetry;
					// Persist the action with the end date
					action.setEndTime(new Timestamp(System.currentTimeMillis()));
					action.setDuration(action.getEndTime().getTime() - action.getStartTime().getTime());
					dataBase.merge(action);
					do {
						Server server = getServer();
						List<Action> actions = server.getActions();
						Iterator<Action> actionIterator = actions.iterator();
						logger.debug("Server : " + server.getActions().size());
						try {
							// Remove the action from the grid
							int retry = 10;
							do {
								while (actionIterator.hasNext()) {
									Action gridAction = actionIterator.next();
									// logger.info("Grid action : " + gridAction);
									if (gridAction.getId() == action.getId()) {
										actionIterator.remove();
										logger.debug("Removed grid action : " + gridAction);
									}
								}
								ThreadUtilities.sleep(1000);
								put(server.getAddress(), server);
							} while (retry-- >= 0);
						} catch (Exception e) {
							logger.error("Exception removing action from cluster : " + e.getMessage(), e);
						}
						server = getServer();
						logger.debug("Action sizes : " + server.getActions().size());
						logger.debug("Server actions : " + server.getActions());
						// Test the grid to see that the action is removed
						Comparator<Action> comparator = new Comparator<Action>() {
							@Override
							public int compare(final Action o1, final Action o2) {
								return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
							}
						};
						Collections.sort(actions, comparator);
						int insertionPoint = Collections.binarySearch(actions, action, comparator);
						if (insertionPoint >= 0) {
							if (maxRetry-- > 0) {
								logger.warn("Didn't remove action : " + action + ", retrying : ");
								ThreadUtilities.sleep(1000);
							} else {
								logger.warn("Ran out of attempts to remove action : " + action);
								logger.warn("Grid server : " + ToStringBuilder.reflectionToString(server));
								break;
							}
						} else {
							logger.debug("Removed action : " + action + ", " + server);
							break;
						}
					} while (true);
				}
			}
			Future<?> future = ThreadUtilities.submitSystem(new RetryCaller());
			ThreadUtilities.waitForFuture(future, 10);
		} finally {
			notifyAll();
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
		Server server = (Server) get(address);
		if (server == null) {
			server = new Server();
			dataBase.persist(server);
			logger.info("Server null, creating new one : " + server);
		}
		server.setIp(ip);
		server.setAddress(address);
		server.setAge(System.currentTimeMillis());

		Collection<IndexContext> collection = monitorService.getIndexContexts().values();
		List<IndexContext> indexContexts = new ArrayList<IndexContext>(collection);
		server.setIndexContexts(indexContexts);

		return server;
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
	public <T> T get(final Object key) {
		return (T) Hazelcast.getMap(IConstants.IKUBE).get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(final Object key, final Object value) {
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