package ikube.cluster.cache;

import ikube.database.IDataBase;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hazelcast.core.MapStore;

public class DataBaseOdbMapStore<T> implements MapStore<Long, T> {

	private IDataBase dataBase;

	public DataBaseOdbMapStore() {
		dataBase = ApplicationContextManager.getBean(IDataBase.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T load(Long key) {
		return (T) dataBase.find(Object.class, key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<Long, T> loadAll(Collection<Long> keys) {
		List<Url> list = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
		Map<Long, Url> map = new HashMap<Long, Url>();
		for (Url url : list) {
			map.put(url.getId(), url);
		}
		return (Map<Long, T>) map;
	}

	@Override
	public void store(Long key, T value) {
		Url url = dataBase.find(Url.class, key);
		if (url == null) {
			dataBase.persist(value);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void storeAll(Map<Long, T> map) {
		for (Long key : map.keySet()) {
			T url = (T) dataBase.find(Object.class, key);
			if (url == null) {
				dataBase.persist(map.get(key));
			}
		}
	}

	@Override
	public void delete(Long key) {
		Url url = dataBase.find(Url.class, key);
		if (url != null) {
			dataBase.remove(url);
		}
	}

	@Override
	public void deleteAll(Collection<Long> keys) {
		for (Long key : keys) {
			Url url = dataBase.find(Url.class, key);
			if (url != null) {
				dataBase.remove(url);
			}
		}
	}

}