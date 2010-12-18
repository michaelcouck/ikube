package ikube.cluster.cache;

import ikube.database.IDataBase;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DatabaseUtilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hazelcast.core.MapStore;

/**
 * This class will persist the cache to the local file system. Currently this is not very useful but will be when large volumes of internet
 * data is accessed and the cache contains too much data for memory.
 * 
 * @author Michael Couck
 * @since 15.12.10
 * @version 01.00
 */
public class CacheMapStore implements MapStore<Long, Object> {

	private Logger logger = Logger.getLogger(this.getClass());
	/** The database object where the data will be persisted. */
	private IDataBase dataBase;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Object load(Long key) {
		try {
			return getDataBase().find(key);
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Map<Long, Object> loadAll(Collection<Long> keys) {
		try {
			Map<Long, Object> map = new HashMap<Long, Object>();
			for (Long key : keys) {
				Object object = getDataBase().find(key);
				Long id = (Long) DatabaseUtilities.getIdFieldValue(object);
				map.put(id, object);
			}
			return map;
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void store(Long key, Object value) {
		try {
			Long id = (Long) DatabaseUtilities.getIdFieldValue(value);
			Object object = getDataBase().find(value.getClass(), id);
			if (object == null) {
				getDataBase().persist(value);
			}
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void storeAll(Map<Long, Object> map) {
		try {
			for (Long key : map.keySet()) {
				Object object = map.get(key);
				Object persistable = getDataBase().find(object.getClass(), key);
				if (persistable == null) {
					getDataBase().persist(map.get(key));
				}
			}
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void delete(Long key) {
		try {
			Object object = getDataBase().find(key);
			if (object != null) {
				getDataBase().remove(object);
			}
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void deleteAll(Collection<Long> keys) {
		try {
			for (Long key : keys) {
				logger.debug("Key : " + key);
				Object object = getDataBase().find(key);
				logger.debug("Object : " + object);
				if (object != null) {
					getDataBase().remove(object);
				}
			}
		} finally {
			notifyAll();
		}
	}

	private synchronized IDataBase getDataBase() {
		try {
			if (dataBase == null) {
				dataBase = ApplicationContextManager.getBean(IDataBase.class);
			}
			return dataBase;
		} finally {
			notifyAll();
		}
	}

}