package ikube.cluster.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;

public class Cache implements ICache {

	protected Logger logger;
	private Map<String, Map<Long, ?>> maps;

	public void initialise() {
		logger = Logger.getLogger(this.getClass());
		maps = new HashMap<String, Map<Long, ?>>();
	}

	@Override
	public <T> int size(String name) {
		return getMap(name).size();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String name, Long hash) {
		return (T) getMap(name).get(hash);
	}

	@Override
	public <T> void set(String name, Long hash, T t) {
		getMap(name).put(hash, t);
	}

	@Override
	public <T> void remove(String name, Long hash) {
		getMap(name).remove(hash);
	}

	@Override
	public <T> List<T> get(String name, ICriteria<T> criteria, IAction<T> action, int size) {
		List<T> batch = new ArrayList<T>();
		Map<Long, T> map = getMap(name);
		for (Long key : map.keySet()) {
			T t = map.get(key);
			if (criteria != null) {
				boolean evaluated = criteria.evaluate(t);
				if (!evaluated) {
					continue;
				}
			}
			if (action != null) {
				action.execute(t);
			}
			batch.add(t);
			if (batch.size() >= size) {
				break;
			}
		}
		return batch;
	}

	@Override
	public <T> void clear(String name) {
		getMap(name).clear();
	}

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

}