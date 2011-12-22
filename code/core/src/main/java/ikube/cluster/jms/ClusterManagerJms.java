package ikube.cluster.jms;

import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.Action;
import ikube.model.Server;
import ikube.toolkit.ThreadUtilities;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.network.NetworkBridge;
import org.apache.activemq.network.NetworkConnector;
import org.apache.activemq.xbean.XBeanBrokerService;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * This class is the cluster manager implementation using Jms.
 * 
 * During the executions of the rules, we need the cluster to be locked so that one of the other servers doesn't start writing an index and
 * essentially upset the rules executions. This can happen in the case where this server is checking for an index that has expired, finds
 * that the index is expired and starts indexing the data. Between the server having checked for the index expired date and the time the
 * action is set in the cluster to indicate that this index is being generated, another server can start the same index, a race condition,
 * which will cause both servers to create indexes for exactly the same index. We want to avoid duplicate work. As such when the rules are
 * executed and an action is taken, it must happen atomically in the cluster.
 * 
 * @author Michael Couck
 * @since 11.11.11
 * @version 01.00
 */
public class ClusterManagerJms implements IClusterManager, MessageListener {

	/**
	 * This class is the lock class that is passed around the cluster containing the unique address of the server in the cluster and the
	 * shout time, which determines who gets the lock.
	 */
	public static class Lock implements Serializable {

		/** The timestamp for the server that shouted first. */
		protected long shout;
		/** The unique address of the server that sent the lock request. */
		protected String address;
		/** Whether the lock was requested or granted. */
		protected boolean locked;

		public Lock(String address, long shout, boolean locked) {
			this.address = address;
			this.shout = shout;
			this.locked = locked;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ClusterManagerJms.class);

	/** The time to wait for the responses from the cluster servers. */
	private static final long RESPONSE_TIME = 250;

	/** The textual representation of the ip address for this server. */
	private String ip;
	@Value("${activemq.port}")
	private long jmsPort;
	@Value("${activemq.destination}")
	private String destination;
	/** The address or unique identifier for this server. */
	private String address;
	/** The list of locks that have been applied/accepted for. Please refer to the class JavaDoc for more information. */
	private Map<String, Lock> locks;
	/** The list of servers in the cluster. */
	private Map<String, Server> servers;
	@Autowired
	private IDataBase dataBase;
	/** Access to the ActiveMq cluster functionality. */
	@Autowired
	private XBeanBrokerService xBeanBrokerService;
	/** A map of templates to send messages to the cluster. */
	private Map<String, JmsTemplate> jmsTemplates;

	/**
	 * This method will post a message to all servers, with a lock containing the time the lock was requested. It will then wait a short
	 * time for responses. Then the server that made the request first will get the lock. Generally though most servers will not be
	 * competing for the lock.
	 * 
	 * @see ClusterManagerJms
	 */
	@Override
	public synchronized boolean lock(String name) {
		try {
			if (isLocked()) {
				return Boolean.FALSE;
			}
			long shout = System.currentTimeMillis();
			// Send the lock with the locked flag to try get the lock from the cluster
			Lock lock = getLock(address, shout, Boolean.TRUE);
			sendMessage(lock);
			ThreadUtilities.sleep(RESPONSE_TIME);
			boolean haveLock = haveLock(shout);
			if (haveLock) {
				// If we have the lock, i.e. we were first to shout then
				// we do nothing and the other servers will reset their locks to false
				LOGGER.debug("Got lock : {} : {}", address, lock);
			} else {
				// If we don't get the lock then we reset our lock to false
				// for the whole cluster
				getLock(address, Long.MAX_VALUE, Boolean.FALSE);
				sendMessage(lock);
				LOGGER.debug("Didn't get lock : {} : {} : ", address, lock);
			}
			return haveLock;
		} finally {
			notifyAll();
		}
	}

	/**
	 * This method will unlock the cluster. We only unlock the cluster if we already have the lock of course.
	 */
	@Override
	public synchronized boolean unlock(String name) {
		try {
			Lock lock = locks.get(address);
			if (lock.locked) {
				// We only set the lock for the cluster to false if we have it
				lock = getLock(address, Long.MAX_VALUE, Boolean.FALSE);
				sendMessage(lock);
				LOGGER.debug("Unlock : {} : {} : ", address, locks);
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	/**
	 * Checks to see if any server is currently holding the lock for the cluster.
	 * 
	 * @return whether any server has been granted the lock
	 */
	private boolean isLocked() {
		for (Lock stackLock : locks.values()) {
			if (stackLock.locked) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * This method checks to see if we have the lock. When a server wants the lock it posts the {@link Lock} object to the cluster. Other
	 * servers that may or may not be competing for the lock also post their locks. Whoever shouts for the lock first then get it. This
	 * method checks to see if this server shouted first based on the locks from the other servers in the cluster.
	 * 
	 * @param shout the time that this server shouted for the lock
	 * @return whether this server shouted for the lock first thereby getting the lock for the cluster
	 */
	protected boolean haveLock(long shout) {
		for (Entry<String, Lock> entry : locks.entrySet()) {
			if (!entry.getValue().locked) {
				continue;
			}
			if (entry.getKey().equals(address)) {
				continue;
			}
			if (entry.getValue().shout <= shout) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	/**
	 * This method just listens on a topic for messages. Typically messages will contain locks and server objects. These objects from remote
	 * servers will then be inserted into the locks and servers maps, updating the data on this server.
	 */
	@Override
	public void onMessage(Message message) {
		try {
			if (ObjectMessage.class.isAssignableFrom(message.getClass())) {
				ObjectMessage objectMessage = (ObjectMessage) message;
				Object object = objectMessage.getObject();
				if (Lock.class.isAssignableFrom(object.getClass())) {
					Lock lock = (Lock) object;
					locks.put(lock.address, lock);
				} else if (Server.class.isAssignableFrom(object.getClass())) {
					Server server = (Server) object;
					servers.put(server.getAddress(), server);
					debug(server);
				}
			} else {
				LOGGER.warn("Message type not supported : {} ", message);
			}
		} catch (Exception e) {
			LOGGER.error("Exception getting message : ", e);
		} finally {
			// This dead locks
			// notifyAll();
		}
	}

	/**
	 * This method will send messages to the whole cluster.
	 * 
	 * @param serializable the object to send in the message
	 */
	@Override
	public void sendMessage(final Serializable serializable) {
		MessageCreator messageCreator = new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createObjectMessage(serializable);
			}
		};
		Broker broker;
		try {
			broker = xBeanBrokerService.getBroker();
		} catch (Exception e) {
			throw new RuntimeException("Exception accessing the Jms broker from the mBean : " + xBeanBrokerService, e);
		}
		BrokerService brokerService = broker.getBrokerService();
		List<NetworkConnector> networkConnectors = brokerService.getNetworkConnectors();
		for (NetworkConnector networkConnector : networkConnectors) {
			Collection<NetworkBridge> networkBridges = networkConnector.activeBridges();
			for (NetworkBridge networkBridge : networkBridges) {
				String address = networkBridge.getRemoteAddress();
				try {
					LOGGER.debug("Remote address : {} ", address);
					JmsTemplate jmsTemplate = getJmsTemplate(address);
					jmsTemplate.send(messageCreator);
				} catch (Exception e) {
					LOGGER.error("Exception sending message to : " + address + ", will remove template : ", e);
					closeConnection(address);
				}
			}
		}
	}

	private void closeConnection(String address) {
		try {
			JmsTemplate jmsTemplate = jmsTemplates.remove(address);
			if (jmsTemplate != null) {
				ConnectionFactory connectionFactory = jmsTemplate.getConnectionFactory();
				if (connectionFactory != null) {
					connectionFactory.createConnection().close();
				}
				LOGGER.info("Failed connection object : " + connectionFactory);
			} else {
				LOGGER.warn("Jms template was null : " + address);
			}
		} catch (Exception e) {
			LOGGER.error("Exception removing Jms template to cluster node : " + address, e);
		}
	}

	private JmsTemplate getJmsTemplate(String address) {
		if (jmsTemplates.get(address) != null) {
			return jmsTemplates.get(address);
		}
		String url = "tcp:/" + address;
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
		JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
		jmsTemplate.setDefaultDestinationName(destination);
		jmsTemplates.put(address, jmsTemplate);
		return jmsTemplate;
	}

	/**
	 * Creates the lock if it is not already instantiated.
	 * 
	 * @param address the address of this server
	 * @param shout the time the server shouted for the lock
	 * @param locked whether this is a request for the lock or a reset of false to release the lock
	 * @return the lock for this server
	 */
	protected Lock getLock(String address, long shout, boolean locked) {
		Lock lock = locks.get(address);
		if (lock == null) {
			lock = new Lock(address, shout, locked);
		}
		lock.shout = shout;
		lock.locked = locked;
		locks.put(address, lock);
		return lock;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean anyWorking() {
		for (Server server : servers.values()) {
			if (server.isWorking()) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean anyWorking(String indexName) {
		for (Server server : servers.values()) {
			List<Action> actions = server.getActions();
			for (Action action : actions) {
				if (action != null && action.getEndTime() == null && indexName.equals(action.getIndexName())) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized long startWorking(String actionName, String indexName, String indexableName) {
		try {
			Action action = getAction(indexName, actionName, indexableName, Boolean.TRUE);
			Server server = getServer();
			server.getActions().add(action);
			LOGGER.info("Start working : {} ", action);
			sendMessage(server);
			return action.getId();
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void stopWorking(long id) {
		try {
			Server server = getServer();
			List<Action> actions = server.getActions();
			Action action = null;
			int index = 0;
			for (int i = index; i < actions.size(); i++, index++) {
				Action serverAction = actions.get(i);
				if (serverAction.getId() == id) {
					action = serverAction;
					break;
				}
			}
			if (action != null) {
				action.setEndTime(new Timestamp(System.currentTimeMillis()));
				action.setDuration(action.getEndTime().getTime() - action.getStartTime().getTime());
				LOGGER.info("Stop working : {} ", action);
				server.getActions().remove(index);
				dataBase.merge(action);
				sendMessage(server);
			} else {
				LOGGER.warn("Action not found : {} ", id );
			}
		} finally {
			notifyAll();
		}
	}

	private Action getAction(String indexName, String actionName, String indexableName, boolean working) {
		Action action = new Action();
		action.setActionName(actionName);
		action.setDuration(0);
		action.setIndexableName(indexableName);
		action.setIndexName(indexName);
		action.setStartTime(new Timestamp(System.currentTimeMillis()));
		// Must persist the action to get an id
		dataBase.persist(action);
		return action;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Server> getServers() {
		return servers;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Lock> getLocks() {
		return locks;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Server getServer() {
		Server server = servers.get(address);
		if (server == null) {
			server = new Server();
		}
		long time = System.currentTimeMillis();
		server.setIp(ip);
		server.setId(time);
		server.setAge(time);
		server.setAddress(address);
		servers.put(server.getAddress(), server);
		return server;
	}

	public void initialize() throws UnknownHostException {
		locks = new HashMap<String, Lock>();
		servers = new HashMap<String, Server>();
		jmsTemplates = new HashMap<String, JmsTemplate>();
		ip = InetAddress.getLocalHost().getHostAddress();
		address = ip + "." + jmsPort;

		getServer();
		getLock(address, Long.MAX_VALUE, Boolean.FALSE);
	}

	public void destroy() {
		this.servers.clear();
		this.jmsTemplates.clear();
	}

	private void debug(Server server) {
		if (LOGGER.isDebugEnabled()) {
			List<Action> actions = server.getActions();
			if (actions.size() > 0) {
				for (Action serverAction : actions) {
					if (serverAction.getEndTime() == null) {
						LOGGER.debug("        still working : {} {} {}", new Object[] { serverAction.getId(), serverAction.getActionName(),
								serverAction.getIndexName() });
					}
				}
			}
		}
	}

}