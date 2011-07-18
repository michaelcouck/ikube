package ikube.cluster.cache;

import ikube.database.IDataBase;

import java.util.ArrayList;
import java.util.Collection;
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

	private transient Logger logger;
	/** The underlying database. */
	private transient IDataBase dataBase;

	public void initialise() {
		logger = Logger.getLogger(this.getClass());
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
		T object = (T) getMap(name).get(id);
		if (object == null && dataBase != null) {
			try {
				// Try the underlying database
				// TODO This needs to be changed! The name is the name
				// of the map, not the class name, API change in fact!
				object = (T) dataBase.find(Class.forName(name), id);
				if (object != null) {
					getMap(name).put(id, object);
				}
			} catch (ClassNotFoundException e) {
				logger.error("Exception looking for object : " + name + ", " + id, e);
			}
		}
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Object> void set(final String name, final Long id, final T object) {
		getMap(name).put(id, object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(final String name, final Long id) {
		getMap(name).remove(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Object> List<T> get(final String name, final ICriteria<T> criteria, final IAction<T> action, final int size) {
		// This method returns a batch of objects determined by the
		// criteria. In the case of urls it will iterate through ALL the urls in the map. The logic to access
		// a batch of urls must be changed, when a url is indexed then it should be moved to another map
		// and only the not yet crawled urls will remain in this map. Anything up to 100 000 urls is fine, but
		// 1 000 000 000 could be a small problem.
		List<T> list = new ArrayList<T>();
		Map<Long, T> map = getMap(name);
		for (T object : map.values()) {
			if (list.size() >= size) {
				break;
			}
			if (criteria != null) {
				// The result from the criteria evaluation determines whether
				// the object will be included in the result batch returned
				boolean evaluated = criteria.evaluate(object);
				if (!evaluated) {
					continue;
				}
			}
			if (action != null) {
				action.execute(object);
			}
			list.add(object);
		}
		return list;
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
		Map<Long, T> map = getMap(name);
		Collection<T> collection = ((IMap<Long, T>) map).values(new SqlPredicate(sql));
		if (collection.isEmpty()) {
			return null;
		}
		return collection.iterator().next();
	}

	private <T extends Object> Map<Long, T> getMap(final String name) {
		return Hazelcast.getMap(name);
	}

	public void setDataBase(final IDataBase dataBase) {
		this.dataBase = dataBase;
	}

}