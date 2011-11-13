package ikube.cluster.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class CacheLocal implements ICache {

	private boolean locked;
	private Map<String, Map<Long, Object>> masterCache;

	public CacheLocal() {
		masterCache = new HashMap<String, Map<Long, Object>>();
	}

	@Override
	public int size(String name) {
		return getMap(name).size();
	}

	@Override
	public void clear(String name) {
		getMap(name).clear();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String name, Long id) {
		return (T) getMap(name).get(id);
	}

	@Override
	public <T> T get(String name, String sql) {
		throw new RuntimeException("This operation is not really required : ");
	}

	@Override
	public <T> void set(String name, Long id, T object) {
		getMap(name).put(id, object);
	}

	@Override
	public void remove(String name, Long id) {
		getMap(name).remove(id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> get(String name, ICriteria<T> criteria, IAction<T> action, int size) {
		List<T> result = new ArrayList<T>();
		Map<Long, Object> map = getMap(name);
		for (Map.Entry<Long, Object> mapEntry : map.entrySet()) {
			if (result.size() >= size) {
				break;
			}
			T t = (T) mapEntry.getValue();
			if (criteria == null) {
				result.add(t);
			} else {
				if (criteria.evaluate(t)) {
					result.add(t);
				}
			}
			if (action != null) {
				action.execute(t);
			}
		}
		return result;
	}

	@Override
	public synchronized boolean lock(String name) {
		if (locked) {
			return Boolean.FALSE;
		}
		locked = Boolean.TRUE;
		return Boolean.TRUE;
	}

	@Override
	public synchronized boolean unlock(String name) {
		if (locked) {
			locked = Boolean.FALSE;
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	private Map<Long, Object> getMap(String name) {
		Map<Long, Object> cache = masterCache.get(name);
		if (cache == null) {
			cache = new HashMap<Long, Object>();
			masterCache.put(name, cache);
		}
		return cache;
	}

}
