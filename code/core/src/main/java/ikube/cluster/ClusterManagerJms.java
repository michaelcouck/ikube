package ikube.cluster;

import ikube.cluster.cache.ICache.IAction;
import ikube.cluster.cache.ICache.ICriteria;
import ikube.model.Action;
import ikube.model.Server;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class ClusterManagerJms implements IClusterManager, MessageListener {

	public static class Lock implements Serializable {
		public String ip;
		public long shout;
		public boolean lock;

		public Lock(String ip, boolean lock, long shout) {
			this.ip = ip;
			this.lock = lock;
			this.shout = shout;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).concat(new Date(shout).toString());
		}
	}

	private static final Logger LOGGER = Logger.getLogger(ClusterManagerJms.class);

	protected String ip;
	private Server server;
	private Map<String, Server> servers;
	protected Map<String, Lock> locks;
	private Map<String, Action> actions;
	private JmsTemplate jmsTemplate;

	public ClusterManagerJms() throws UnknownHostException, InterruptedException {
		servers = new HashMap<String, Server>();
		locks = new HashMap<String, ClusterManagerJms.Lock>();
		actions = new HashMap<String, Action>();
		ip = InetAddress.getLocalHost().getHostAddress() + ":" + Thread.currentThread().hashCode();
	}

	@Override
	public synchronized boolean lock(String name) {
		try {
			if (isLocked()) {
				return Boolean.FALSE;
			}
			LOGGER.info(ip + " : " + locks);
			Lock lock = getLock(ip, true, System.currentTimeMillis());
			sendMessage((Serializable) lock);
			waitForResponse();
			boolean haveLock = haveLock(lock.shout);
			if (haveLock) {
				LOGGER.info(ip + " : got lock : " + locks);
			} else {
				LOGGER.info(ip + " : didn't get lock : " + locks);
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
				locks.put(lock.ip, lock);
			} else if (Action.class.isAssignableFrom(object.getClass())) {
				Action action = (Action) object;
				actions.put(action.getServerName(), action);
			} else if (Server.class.isAssignableFrom(object.getClass())) {
				Server server = (Server) object;
				servers.put(server.getAddress(), server);
			}
			LOGGER.info(object);
		} catch (Exception e) {
			LOGGER.error("Exception getting message : ", e);
		}
	}

	@Override
	public synchronized boolean unlock(String name) {
		try {
			LOGGER.info(ip + " : " + locks);
			Lock lock = locks.get(ip);
			if (lock != null && lock.lock) {
				sendMessage((Serializable) lock);
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	private boolean isLocked() {
		for (Lock stackLock : locks.values()) {
			if (stackLock.lock) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	protected boolean haveLock(long shout) {
		Lock lock = locks.get(ip);
		if (!lock.lock) {
			return Boolean.FALSE;
		}
		for (Lock stackLock : locks.values()) {
			if (!stackLock.lock) {
				continue;
			}
			if (stackLock.ip.equals(ip)) {
				continue;
			}
			if (stackLock.shout <= lock.shout) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	private void waitForResponse() {
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			LOGGER.error("", e);
		}
	}

	protected void sendMessage(final Serializable serializable) {
		jmsTemplate.send(new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createObjectMessage(serializable);
			}
		});
	}

	private Lock getLock(String ip, boolean mustLock, long shout) {
		Lock lock = locks.get(ip);
		if (lock == null) {
			lock = new Lock(ip, mustLock, shout);
			locks.put(ip, lock);
		}
		lock.lock = mustLock;
		lock.shout = shout;
		return lock;
	}

	@Override
	public boolean anyWorking() {
		for (Lock stackLock : locks.values()) {
			if (stackLock.lock) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	@Override
	public boolean anyWorking(String indexName) {
		for (Action action : actions.values()) {
			if (indexName.equals(action.getIndexName())) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	@Override
	public long startWorking(String actionName, String indexName, String indexableName) {
		Action action = getAction(indexName, actionName, indexableName, Boolean.TRUE);
		sendMessage((Serializable) action);
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
		sendMessage((Serializable) action);
	}

	@Override
	public long getIdNumber(String indexableName, String indexName, long batchSize, long minId) {
		for (Action action : actions.values()) {
			if (action.getWorking() && action.getIndexableName().equals(indexableName) && action.getIndexName().equals(indexName)) {
				long idNumber = action.getIdNumber();
				action.setIdNumber(idNumber + batchSize);
				return idNumber;
			}
		}
		return 0;
	}

	@Override
	public List<Server> getServers() {
		List<Server> servers = new ArrayList<Server>();
		for (Server server : this.servers.values()) {
			servers.add(server);
		}
		return servers;
	}

	@Override
	public Server getServer() {
		if (server == null) {
			server = new Server();
			server.setAddress(ip);
			server.setAge(System.currentTimeMillis());
			server.setId(System.nanoTime());
			server.setIp(ip);
		}
		return server;
	}

	@Override
	public <T> T get(String name, Long id) {
		return null;
	}

	@Override
	public <T> List<T> get(Class<T> klass, String name, ICriteria<T> criteria, IAction<T> action, int size) {
		return null;
	}

	@Override
	public <T> void set(String name, Long id, T object) {
	}

	@Override
	public <T> void clear(String name) {
	}

	@Override
	public <T> int size(String klass) {
		return 0;
	}

	@Override
	public <T> void remove(String name, Long id) {
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