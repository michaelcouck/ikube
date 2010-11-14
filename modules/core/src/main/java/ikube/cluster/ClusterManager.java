package ikube.cluster;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.Lock;
import ikube.model.Server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

public class ClusterManager extends ReceiverAdapter implements IClusterManager {

	protected static final String CONFIGURATION = "META-INF/cluster/mping.xml";

	private Logger logger = Logger.getLogger(ClusterManager.class);
	private JChannel channel;
	private Map<String, Server> servers = new HashMap<String, Server>();
	private Lock lock;
	private long lockTimeout = 1000;

	public ClusterManager() {
		try {
			channel = new JChannel(CONFIGURATION);
			channel.enableStats(Boolean.TRUE);
			channel.connect(IConstants.IKUBE);
			channel.setReceiver(this);
		} catch (Exception e) {
			logger.error("", e);
		}
		lock = new Lock();
		lock.setClassName(null);
		lock.setLocked(Boolean.FALSE);
		lock.setStart(0);
	}

	@Override
	public synchronized void receive(Message message) {
		Object object = message.getObject();
		logger.info("Message : " + object);
		if (object != null) {
			if (Server.class.isAssignableFrom(object.getClass())) {
				Server server = (Server) object;
				servers.put(server.getIp(), server);
				release();
			}
		}
		notifyAll();
	}

	@Override
	public synchronized Server getServer(IndexContext indexContext) {
		lock();
		Server thisServer = servers.get(indexContext.getServerName());
		if (thisServer == null) {
			thisServer = new Server();
			thisServer.setIp(indexContext.getServerName());
			publish(thisServer);
		}
		notifyAll();
		return thisServer;
	}

	@Override
	public synchronized boolean anyWorking(IndexContext indexContext, String actionName) {
		lock();
		try {
			logger.info("Action : " + actionName + ", " + Thread.currentThread().hashCode());
			for (Server server : servers.values()) {
				if (server.isWorking() && !actionName.equals(server.getAction())) {
					return Boolean.TRUE;
				}
			}
		} finally {
			release();
			notifyAll();
		}
		return Boolean.FALSE;
	}

	@Override
	public synchronized boolean areWorking(IndexContext indexContext, String actionName) {
		lock();
		try {
			logger.info("Action : " + actionName);
			for (Server server : servers.values()) {
				if (server.isWorking() && indexContext.getIndexName().equals(server.getIndex()) && actionName.equals(server.getAction())) {
					return Boolean.TRUE;
				}
			}
		} finally {
			release();
			notifyAll();
		}
		return Boolean.FALSE;
	}

	@Override
	public synchronized long getLastWorkingTime(IndexContext indexContext, String actionName) {
		lock();
		long time = System.currentTimeMillis();
		try {
			logger.info("Servers : " + servers + ", " + Thread.currentThread().hashCode());
			Server thisServer = getServer(indexContext);
			for (Server server : servers.values()) {
				if (!server.isWorking()) {
					continue;
				}
				if (!indexContext.getIndexName().equals(server.getIndex())) {
					continue;
				}
				// This means that there is a server working on this index but with
				// a different action
				if (!actionName.equals(server.getAction())) {
					time = -1;
					break;
				}
				long start = server.getStart();
				// We want the start time of the first server, so if the start time
				// of this server is lower than the existing start time then take that
				if (start < time && start > 0) {
					logger.info("Taking start time of server : " + server);
					time = start;
				}
			}
			thisServer.setStart(time);
			thisServer.setAction(actionName);
			thisServer.setBatch(0);
			thisServer.setWorking(Boolean.TRUE);
			publish(thisServer);
			logger.info("Time : " + time + ", " + servers + ", " + Thread.currentThread().hashCode());
		} finally {
			// release();
			notifyAll();
		}
		return time;
	}

	@Override
	public synchronized int getNextBatchNumber(IndexContext indexContext) {
		lock();
		int batch = 0;
		try {
			String index = indexContext.getIndexName();
			Server thisServer = getServer(indexContext);
			for (Server server : servers.values()) {
				if (server.isWorking() && index.equals(server.getIndex())) {
					if (server.getBatch() > batch) {
						batch = server.getBatch();
					}
				}
			}
			logger.info("Batch : " + batch + ", " + thisServer);
			// Set the batch for this server to the batch plus
			// the increment in the context
			thisServer.setBatch(batch + (int) indexContext.getBatchSize());
			// Publish this server to the cluster
			publish(thisServer);
		} finally {
			// release();
			notifyAll();
		}
		return batch;
	}

	@Override
	public Map<String, Server> getServers(IndexContext indexContext) {
		return servers;
	}

	@Override
	public synchronized Boolean isWorking(IndexContext indexContext) {
		lock();
		try {
			Server thisServer = getServer(indexContext);
			logger.info("This server : " + thisServer);
			return thisServer != null && thisServer.isWorking();
		} finally {
			release();
			notifyAll();
		}
	}

	@Override
	public synchronized boolean resetWorkings(IndexContext indexContext, String actionName) {
		lock();
		try {
			// Check that there are no servers working
			for (Server server : servers.values()) {
				if (server.isWorking()) {
					return Boolean.FALSE;
				}
			}
			logger.info("Servers : " + servers);
			List<Server> synchronizedServers = Arrays.asList(servers.values().toArray(new Server[servers.size()]));
			for (Server server : synchronizedServers) {
				server.setAction(null);
				server.setBatch(0);
				server.setStart(0);
				server.setWorking(Boolean.FALSE);
				publish(server);
				lock();
			}
			return Boolean.TRUE;
		} finally {
			// release();
			notifyAll();
		}
	}

	@Override
	public synchronized void setWorking(IndexContext indexContext, String actionName, boolean isWorking, long start) {
		lock();
		try {
			Server thisServer = getServer(indexContext);
			thisServer.setIndex(indexContext.getIndexName());
			thisServer.setWorking(isWorking);
			thisServer.setAction(actionName);
			thisServer.setStart(start);
			publish(thisServer);
			logger.info("This server : " + thisServer + ", " + servers);
		} finally {
			// release();
			notifyAll();
		}
	}

	protected synchronized Lock lock() {
		try {
			while (lock.isLocked()) {
				try {
					notifyAll();
					wait(100);
				} catch (InterruptedException e) {
					logger.error("Interrupted : ", e);
				}
				if (lock.isLocked() && System.currentTimeMillis() - lock.getStart() > lockTimeout) {
					break;
				}
			}
			lock.setStart(System.currentTimeMillis());
			lock.setLocked(Boolean.TRUE);
		} finally {
			notifyAll();
		}
		return lock;
	}

	protected synchronized void release() {
		try {
			lock.setLocked(Boolean.FALSE);
			lock.setStart(0);
			lock.setClassName(null);
		} finally {
			notifyAll();
		}
	}

	protected synchronized void publish(Server server) {
		try {
			Message message = new Message();
			message.setObject(server);
			channel.send(message);
		} catch (Exception e) {
			logger.error("Exception publishing the server to the cluster : " + server, e);
		}
	}

}