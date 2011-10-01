package ikube.cluster.cache;

import ikube.IConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.ReplicatedHashMap;
import org.jgroups.blocks.locking.LockService;

/**
 * JGroups does not have a distributed map unfortunately.
 * 
 * @see ICache
 * @author Michael Couck
 * @since 01.10.11
 * @version 01.00
 */
class DMap implements Serializable {
	public String				name;
	public Map<Long, Object>	map;

	public DMap(String name, Map<Long, Object> map) {
		this.name = name;
		this.map = map;
	}
}

public class CacheJGroups implements ICache {

	private Logger							logger;
	private JChannel						channel;
	private LockService						lockService;
	private Map<String, Map<Long, Object>>	maps;

	public void initialise() throws Exception {
		maps = new HashMap<String, Map<Long, Object>>();
		logger = Logger.getLogger(this.getClass());
		channel = new JChannel(getClass().getResource(IConstants.META_INF + IConstants.SEP + IConstants.UDP_XML));
		channel.setDiscardOwnMessages(Boolean.TRUE);
		channel.connect(IConstants.IKUBE);
		channel.setReceiver(new ReceiverAdapter() {
			public void viewAccepted(View view) {
				logger.info("View : " + view);
			}

			public void receive(Message msg) {
				Address sender = msg.getSrc();
				logger.info("Message : " + msg.getObject() + ", sender : " + sender + ".");
				// DMap map = (DMap) msg.getObject();
				// maps.put(map.name, map.map);
			}
		});
		channel.send(null, "Ikube running : ");
		lockService = new LockService(channel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size(final String name) {
		return getMap(name).size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Object> T get(final String name, final Long id) {
		return (T) getMap(name).get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Object> void set(final String name, final Long id, final T object) {
		getMap(name).put(id, object);
		distribute(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(final String name, final Long id) {
		getMap(name).remove(id);
		distribute(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Object> List<T> get(final String name, final ICriteria<T> criteria, final IAction<T> action, final int size) {
		List<T> result = new ArrayList<T>();
		Map<Long, Object> map = getMap(name);
		for (Map.Entry<Long, Object> mapEntry : map.entrySet()) {
			if (result.size() >= size) {
				break;
			}
			T t = (T) mapEntry.getValue();
			if (criteria != null && criteria.evaluate(t)) {
				result.add(t);
			} else {
				result.add(t);
			}
			if (action != null) {
				action.execute(t);
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear(final String name) {
		getMap(name).clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Object> T get(final String name, final String sql) {
		throw new RuntimeException("This operation is not really required : ");
	}

	public boolean lock(final String name) {
		Lock lock = lockService.getLock(name);
		if (lock != null) {
			try {
				boolean gotLock = lock.tryLock(3000, TimeUnit.MILLISECONDS);
				// logger.info("Got lock : " + gotLock);
				return gotLock;
			} catch (InterruptedException e) {
				logger.error("Exception acquiring the cluster lock : " + name, e);
			}
		}
		// logger.info("Didn't get lock : ");
		return Boolean.FALSE;
	}

	public boolean unlock(String name) {
		Lock lock = lockService.getLock(name);
		if (lock != null) {
			lock.unlock();
			// logger.info("Unlocked : ");
			return Boolean.TRUE;
		}
		// logger.info("Couldn't unlock : ");
		return Boolean.FALSE;
	}

	private void distribute(final String name) {
		// Map<Long, Object> map = getMap(name);
		// DMap dmap = new DMap(name, map);
		// Message message = new Message();
		// message.setObject(dmap);
		try {
			// channel.send(message);
		} catch (Exception e) {
			logger.error("Exception distributing the map data : ", e);
		}
	}

	private Map<Long, Object> getMap(String name) {
		Map<Long, Object> map = maps.get(name);
		if (map == null) {
			// map = new HashMap<Long, Object>();
			map = new ReplicatedHashMap<Long, Object>(channel);
			maps.put(name, map);
		}
		return map;
	}

}