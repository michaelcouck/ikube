package ikube.database.mem;

import ikube.database.IDataBase;
import ikube.toolkit.DatabaseUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @see IDataBase
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class DataBaseMem implements IDataBase {

	private Logger logger;

	private IDataBase dataBase;
	private Map<Class<?>, Map<Long, Object>> cache;

	public DataBaseMem() {
		this.logger = Logger.getLogger(this.getClass());
		this.cache = new HashMap<Class<?>, Map<Long, Object>>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> T persist(T object) {
		try {
			Long idFieldValue = (Long) DatabaseUtilities.getIdFieldValue(object);
			if (idFieldValue == null || idFieldValue == 0) {
				DatabaseUtilities.setIdField(object, System.nanoTime());
			}
			// Check for duplicate keys
			Map<Long, Object> map = getMap(object.getClass());
			Object duplicate = map.get(idFieldValue);
			if (duplicate != null) {
				logger.info("Duplicate key : " + duplicate);
				return object;
			}
			// Save the object in the maps
			map.put(idFieldValue, object);
			return dataBase.persist(object);
		} catch (Exception e) {
			logger.error("Exception persisting object : " + object, e);
			return object;
		} finally {
			notifyAll();
		}
	}

	protected Map<Long, Object> getMap(Class<?> klass) {
		Map<Long, Object> map = cache.get(klass);
		if (map == null) {
			map = new HashMap<Long, Object>();
			cache.put(klass, map);
		}
		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> T remove(T t) {
		try {
			Long idFieldValue = (Long) DatabaseUtilities.getIdFieldValue(t);
			// Remove the object from the maps
			Map<Long, Object> map = getMap(t.getClass());
			map.remove(idFieldValue);
			return dataBase.remove(t);
		} catch (Exception e) {
			logger.error("Exception removing object : " + t, e);
			return t;
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> T remove(Class<T> klass, Long id) {
		try {
			Map<Long, Object> map = getMap(klass);
			map.remove(id);
			return dataBase.remove(klass, id);
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> T merge(T t) {
		try {
			// T is a live object so there is no merge required
			return dataBase.merge(t);
		} catch (Exception e) {
			logger.error("Exception merging object : " + t, e);
			return t;
		} finally {
			notifyAll();
		}
	}

	/**
	 * Note: This method is EXPENSIVE! Use only in emergency.
	 */
	@Override
	public synchronized <T> T find(Long id) {
		try {
			return dataBase.find(id);
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public synchronized <T> T find(Class<T> klass, Long id) {
		try {
			Map<Long, Object> map = getMap(klass);
			T t = (T) map.get(id);
			return t;
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> T find(Class<T> klass, Map<String, Object> parameters, boolean unique) {
		try {
			return dataBase.find(klass, parameters, unique);
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public synchronized <T> List<T> find(Class<T> klass, int startIndex, int endIndex) {
		List<T> list = new ArrayList<T>();
		try {
			Map<Long, Object> map = getMap(klass);
			int index = 0;
			for (Object object : map.values()) {
				if (index >= startIndex) {
					list.add((T) object);
				}
				if (list.size() >= endIndex) {
					break;
				}
				index++;
			}
			return list;
		} catch (Exception e) {
			logger.error("Exception finding object : " + klass + ", " + startIndex + ", " + endIndex, e);
			return list;
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized <T> List<T> find(Class<T> klass, Map<String, Object> parameters, int startIndex, int endIndex) {
		List<T> list = new ArrayList<T>();
		try {
			return dataBase.find(klass, parameters, startIndex, endIndex);
		} catch (Exception e) {
			logger.error("Exception finding objects : " + klass + ", " + parameters + ", " + startIndex + ", " + endIndex, e);
		} finally {
			notifyAll();
		}
		return list;
	}

	public void setDataBase(IDataBase dataBase) {
		this.dataBase = dataBase;
	}

}