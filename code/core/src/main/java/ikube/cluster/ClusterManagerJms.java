package ikube.cluster;

import ikube.IConstants;
import ikube.model.Action;
import ikube.model.Server;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.network.NetworkBridge;
import org.apache.activemq.network.NetworkConnector;
import org.apache.activemq.xbean.XBeanBrokerService;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class ClusterManagerJms implements IClusterManager, MessageListener {

	public static class Lock implements Serializable {

		protected String ip;
		protected long shout;
		protected boolean locked;

		public Lock(String ip, long shout, boolean locked) {
			this.ip = ip;
			this.shout = shout;
			this.locked = locked;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}

	private static final Logger LOGGER = Logger.getLogger(ClusterManagerJms.class);

	private static final long RESPONSE_TIME = 1000;
	private static final long LOCK_TIME_OUT = 5000;
	private static final long SERVER_TIME_OUT = 600000;
	private static final long LOCK_THREAD_WAIT_TIME = 1000;

	private String ip;
	private Server server;
	private Map<String, Lock> locks;
	private JmsTemplate jmsTemplate;
	private Map<String, Server> servers;
	@Autowired
	private XBeanBrokerService xBeanBrokerService;

	public ClusterManagerJms() throws UnknownHostException, InterruptedException {
		locks = new HashMap<String, Lock>();
		servers = new HashMap<String, Server>();
		ip = InetAddress.getLocalHost().getHostAddress() + ":" + Thread.currentThread().hashCode();
		getLock(ip, Long.MAX_VALUE, Boolean.FALSE);
		Thread lockTimeOutThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					List<String> toRemove = new ArrayList<String>();
					for (Entry<String, Lock> entry : locks.entrySet()) {
						if (System.currentTimeMillis() - entry.getValue().shout > LOCK_TIME_OUT) {
							toRemove.add(entry.getKey());
						}
					}
					for (String lockKey : toRemove) {
						LOGGER.warn("Removing lock from time out : " + locks.get(lockKey));
						locks.remove(lockKey);
					}
					sleepForSomeTime(LOCK_THREAD_WAIT_TIME);
				}
			}
		}, "Ikube lock time out thread");
		lockTimeOutThread.start();
		Thread serverTimeOutThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					sleepForSomeTime(SERVER_TIME_OUT);
					Server server = getServer();
					// Remove all servers that are past the max age
					List<Server> toRemove = new ArrayList<Server>();
					List<Server> servers = getServers();
					for (Server remoteServer : servers) {
						if (remoteServer.getAddress().equals(server.getAddress())) {
							continue;
						}
						if (System.currentTimeMillis() - remoteServer.getAge() > IConstants.MAX_AGE) {
							LOGGER.info("Removing server : " + remoteServer + ", "
									+ (System.currentTimeMillis() - remoteServer.getAge() > IConstants.MAX_AGE));
							toRemove.add(remoteServer);
						}
					}
					for (Server toRemoveServer : toRemove) {
						servers.remove(toRemoveServer);
					}
				}
			}
		}, "Ikube server time out thread");
		serverTimeOutThread.start();
	}

	@Override
	public synchronized boolean lock(String name) {
		try {
			sendMessage(getServer());
			if (isLocked()) {
				// LOGGER.info("Is locked : " + ip + ":" + locks);
				// Republish our selves
				return Boolean.FALSE;
			}
			// LOGGER.info("Before : " + locks);
			long shout = System.currentTimeMillis();
			// Send the lock with the locked flag to try get the lock from the cluster
			Lock lock = getLock(ip, shout, Boolean.TRUE);
			sendMessage((Serializable) lock);
			sleepForSomeTime(RESPONSE_TIME);
			boolean haveLock = haveLock(shout);
			if (haveLock) {
				// If we have the lock, i.e. we were first to shout then
				// we do nothing and the other servers will reset their locks to false
				LOGGER.info("Got lock : " + ip + ":" + lock);
			} else {
				// If we don't get the lock then we reset our lock to false
				// for the whole cluster
				getLock(ip, Long.MAX_VALUE, Boolean.FALSE);
				sendMessage((Serializable) lock);
				LOGGER.info("Didn't get lock : " + ip + ":" + lock);
			}
			return haveLock;
		} finally {
			notifyAll();
		}
	}

	@Override
	public void onMessage(Message message) {
		try {
			ObjectMessage objectMessage = (ObjectMessage) message;
			Object object = objectMessage.getObject();
			if (Lock.class.isAssignableFrom(object.getClass())) {
				Lock lock = (Lock) object;
				this.locks.put(lock.ip, lock);
			} else if (Server.class.isAssignableFrom(object.getClass())) {
				Server server = (Server) object;
				servers.put(server.getAddress(), server);
			}
		} catch (Exception e) {
			LOGGER.error("Exception getting message : ", e);
		}
	}

	@Override
	public synchronized boolean unlock(String name) {
		try {
			Lock lock = locks.get(ip);
			if (lock.locked) {
				// We only set the lock for the cluster to false if we have it
				lock = getLock(ip, Long.MAX_VALUE, Boolean.FALSE);
				sendMessage((Serializable) lock);
				LOGGER.info("Unlock : " + ip + ":" + locks);
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	private boolean isLocked() {
		for (Lock stackLock : locks.values()) {
			if (stackLock.locked) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	protected boolean haveLock(long shout) {
		for (Entry<String, Lock> entry : locks.entrySet()) {
			if (!entry.getValue().locked) {
				continue;
			}
			if (entry.getKey().equals(ip)) {
				continue;
			}
			if (entry.getValue().shout <= shout) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	private void sleepForSomeTime(long sleep) {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			LOGGER.error("Exception waiting for the cluster : ", e);
		}
	}

	protected void sendMessage(final Serializable serializable) {
		jmsTemplate.send(new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createObjectMessage(serializable);
			}
		});
		try {
			Broker broker = xBeanBrokerService.getBroker();
			List<NetworkConnector> networkConnectors = broker.getBrokerService().getNetworkConnectors();
			for (NetworkConnector networkConnector : networkConnectors) {
				Collection<NetworkBridge> networkBridges = networkConnector.activeBridges();
				networkConnector.getDynamicallyIncludedDestinations();
				for (NetworkBridge networkBridge : networkBridges) {
					String remoteAddress = networkBridge.getRemoteAddress();
					LOGGER.error("Remote address : " + remoteAddress);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception accessing the cluster network : ", e);
		}
	}

	protected Lock getLock(String ip, long shout, boolean locked) {
		Lock lock = locks.get(ip);
		if (lock == null) {
			lock = new Lock(ip, shout, locked);
			locks.put(ip, lock);
		}
		lock.shout = shout;
		lock.locked = locked;
		return lock;
	}

	@Override
	public boolean anyWorking() {
		for (Server server : servers.values()) {
			Action action = server.getAction();
			if (action != null && action.getWorking()) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	@Override
	public boolean anyWorking(String indexName) {
		for (Server server : servers.values()) {
			Action action = server.getAction();
			if (action != null && action.getWorking() && indexName.equals(action.getIndexName())) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	@Override
	public long startWorking(String actionName, String indexName, String indexableName) {
		Action action = getAction(indexName, actionName, indexableName, Boolean.TRUE);
		Server server = getServer();
		server.setAction(action);
		servers.put(server.getAddress(), server);
		sendMessage((Serializable) server);
		return 0;
	}

	private Action getAction(String indexName, String actionName, String indexableName, boolean working) {
		Action action = new Action();
		action.setActionName(actionName);
		action.setDuration(0);
		action.setId(System.currentTimeMillis());
		action.setIdNumber(0);
		action.setIndexableName(indexableName);
		action.setIndexName(indexName);
		action.setServerName(ip);
		action.setStartTime(new Timestamp(System.currentTimeMillis()));
		action.setWorking(working);
		return action;
	}

	@Override
	public void stopWorking(String actionName, String indexName, String indexableName) {
		Action action = getAction(indexName, actionName, indexableName, Boolean.FALSE);
		Server server = getServer();
		server.setAction(action);
		sendMessage((Serializable) server);
	}

	@Override
	public long getIdNumber(String indexableName, String indexName, long batchSize, long minId) {
		for (Server server : servers.values()) {
			Action action = server.getAction();
			if (action != null && action.getWorking() && indexableName.equals(action.getIndexableName())
					&& indexName.equals(action.getIndexName())) {
				long idNumber = action.getIdNumber();
				if (idNumber < minId) {
					idNumber = minId;
				}
				action.setIdNumber(idNumber + batchSize);
				sendMessage(server);
				return idNumber;
			}
		}
		return 0;
	}

	@Override
	public List<Server> getServers() {
		List<Server> servers = new ArrayList<Server>();
		Collections.addAll(servers, this.servers.values().toArray(new Server[this.servers.values().size()]));
		return servers;
	}

	@Override
	public Server getServer() {
		if (server == null) {
			server = new Server();
		}
		server.setAddress(ip);
		server.setAge(System.currentTimeMillis());
		server.setId(System.nanoTime());
		server.setIp(ip);
		return server;
	}

	@Override
	public boolean isException() {
		return false;
	}

	@Override
	public void setException(boolean exception) {
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

}