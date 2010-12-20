package ikube.cluster.cache;

import ikube.database.IDataBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;

/**
 * @see ICache
 * @author Michael Couck
 * @since 15.12.10
 * @version 01.00
 */
public class Cache implements ICache {

	protected Logger logger;
	/** The underlying database. */
	private IDataBase dataBase;
	/**
	 * This is a map of maps for objects. These maps are propagated throughout the cluster. Typically the name of the map is the name of the
	 * class that it contains, however in some cases this is not convenient.
	 */
	private Map<String, Map<Long, ?>> maps;

	public void initialise() {
		logger = Logger.getLogger(this.getClass());
		maps = new HashMap<String, Map<Long, ?>>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size(String name) {
		return getMap(name).size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String name, Long id) {
		T t = (T) getMap(name).get(id);
		if (t == null) {
			// Try the underlying database
			if (dataBase != null) {
				try {
					t = (T) dataBase.find(Class.forName(name), id);
				} catch (ClassNotFoundException e) {
					logger.error("", e);
				}
			}
		}
		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void set(String name, Long id, T t) {
		getMap(name).put(id, t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(String name, Long id) {
		getMap(name).remove(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> get(String name, ICriteria<T> criteria, IAction<T> action, int size) {
		// This method returns a batch of objects determined by the
		// criteria. In the case of urls it will iterate through ALL the urls in the map. The logic to access
		// a batch of urls must be changed, when a url is indexed then it should be moved to another map
		// and only the not yet crawled urls will remain in this map. Anything up to 100 000 urls is fine, but
		// 1 000 000 000 could be a small problem.
		List<T> list = new ArrayList<T>();
		Map<Long, T> map = getMap(name);
		for (Long id : map.keySet()) {
			T t = map.get(id);
			if (criteria != null) {
				// The result from the criteria evaluation determines whether
				// the object will be included in the result batch returned
				boolean evaluated = criteria.evaluate(t);
				if (!evaluated) {
					continue;
				}
			}
			if (action != null) {
				action.execute(t);
			}
			list.add(t);
			if (list.size() >= size) {
				break;
			}
		}
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear(String name) {
		getMap(name).clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String name, String sql) {
		Map<Long, T> map = getMap(name);
		if (IMap.class.isAssignableFrom(map.getClass())) {
			Collection<Object> collection = ((IMap<Long, T>) map).values(new SqlPredicate(sql));
			if (collection.size() == 0) {
				return null;
			}
			return (T) collection.iterator().next();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <T> Map<Long, T> getMap(String name) {
		Map<Long, T> map = (Map<Long, T>) maps.get(name);
		if (map == null) {
			map = Hazelcast.getMap(name);
			maps.put(name, map);
		}
		return map;
	}

	public void setDataBase(IDataBase dataBase) {
		this.dataBase = dataBase;
	}

}