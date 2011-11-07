package ikube.cluster;

import ikube.cluster.cache.ICache.IAction;
import ikube.cluster.cache.ICache.ICriteria;
import ikube.model.Server;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
		public Date shout;
		public boolean lock;

		public Lock(String ip, boolean lock, Date shout) {
			this.ip = ip;
			this.lock = lock;
			this.shout = shout;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}

	private static final Logger LOGGER = Logger.getLogger(ClusterManagerJms.class);

	private String ip;
	protected Map<String, Lock> locks;
	private JmsTemplate jmsTemplate;

	public ClusterManagerJms() throws UnknownHostException, InterruptedException {
		locks = new HashMap<String, ClusterManagerJms.Lock>();
		ip = InetAddress.getLocalHost().getHostAddress() + ":" + Thread.currentThread().hashCode();
		getLock(ip, false, new Date());
		Thread.sleep((long) (Math.random() * 10000));
	}

	@Override
	public synchronized boolean lock(String name) {
		try {
			if (isLocked()) {
				return Boolean.FALSE;
			}
			LOGGER.info(ip + " : " + locks);
			Date shout = new Date();
			Lock lock = getLock(ip, true, shout);
			sendLock(lock);
			waitForResponse();
			if (!haveLock(shout)) {
				lock = getLock(ip, false, new Date());
				sendLock(lock);
				return Boolean.FALSE;
			}
			LOGGER.info(ip + " : got lock : " + locks);
			return Boolean.TRUE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public void onMessage(Message message) {
		try {
			ObjectMessage objectMessage = (ObjectMessage) message;
			Lock lock = (Lock) objectMessage.getObject();
			locks.put(lock.ip, lock);
			// LOGGER.info(ip + " : " + locks);
		} catch (Exception e) {
			LOGGER.error("Exception getting message : ", e);
		}
	}

	@Override
	public synchronized boolean unlock(String name) {
		try {
			LOGGER.info(ip + " : " + locks);
			Lock lock = getLock(ip, false, new Date());
			sendLock(lock);
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

	private boolean haveLock(Date shout) {
		boolean locked = Boolean.TRUE;
		for (Lock stackLock : locks.values()) {
			if (stackLock.ip.equals(ip) || !stackLock.lock) {
				continue;
			}
			if (stackLock.shout.getTime() <= shout.getTime()) {
				locked = Boolean.FALSE;
			}
		}
		if (locked) {
			for (Lock stackLock : locks.values()) {
				if (stackLock.ip.equals(ip) || !stackLock.lock) {
					continue;
				}
				stackLock.lock = Boolean.FALSE;
			}
		}
		return locked;
	}

	private void waitForResponse() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			LOGGER.error("", e);
		}
	}

	protected void sendLock(final Lock lock) {
		jmsTemplate.send(new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				ObjectMessage objectMessage = session.createObjectMessage((Serializable) lock);
				return objectMessage;
			}
		});
	}

	private Lock getLock(String ip, boolean mustLock, Date shout) {
		Lock lock = locks.get(ip);
		if (lock == null) {
			lock = new Lock(ip, mustLock, shout);
			locks.put(ip, lock);
		} else {
			lock.lock = mustLock;
			lock.shout = shout;
		}
		return lock;
	}

	@Override
	public boolean anyWorking() {
		return false;
	}

	@Override
	public boolean anyWorking(String indexName) {
		return false;
	}

	@Override
	public long startWorking(String actionName, String indexName, String indexableName) {
		return 0;
	}

	@Override
	public void stopWorking(String actionName, String indexName, String indexableName) {
	}

	@Override
	public long getIdNumber(String indexableName, String indexName, long batchSize, long minId) {
		return 0;
	}

	@Override
	public List<Server> getServers() {
		return null;
	}

	@Override
	public Server getServer() {
		return null;
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