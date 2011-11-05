package ikube.cluster;

import ikube.cluster.cache.ICache.IAction;
import ikube.cluster.cache.ICache.ICriteria;
import ikube.model.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class ClusterManagerJms implements IClusterManager, MessageListener, Lock {

	private static final Logger LOGGER = Logger.getLogger(ClusterManagerJms.class);
	private static final String LOCK = "lock";

	private JmsTemplate jmsTemplate;
	private List<Lock> locks = new ArrayList<Lock>();

	public ClusterManagerJms() {

	}

	@Override
	public void onMessage(Message message) {
		LOGGER.error("Message : " + message);
		try {
			Object object = message.getObjectProperty(LOCK);
			if (object != null && Lock.class.isAssignableFrom(object.getClass())) {
				Lock lock = (Lock) object;
				locks.add(lock);
			}
		} catch (JMSException e) {
			LOGGER.error("Exception getting message : ", e);
		}
	}

	@Override
	public boolean lock(String name) {
		jmsTemplate.send(new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage("Lock the cluster if possible");
			}
		});
		// Wait for the acknowledgement
		return false;
	}

	@Override
	public boolean unlock(String name) {
		jmsTemplate.send(new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage("Unlock the cluster if possible");
			}
		});
		return false;
	}

	@Override
	public void lock() {
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
	}

	@Override
	public boolean tryLock() {
		return false;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return false;
	}

	@Override
	public void unlock() {
	}

	@Override
	public Condition newCondition() {
		return null;
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