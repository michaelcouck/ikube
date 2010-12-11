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

	@Override
	@SuppressWarnings("unchecked")
	public T load(Long key) {
		return (T) getDataBase().find(Object.class, key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<Long, T> loadAll(Collection<Long> keys) {
		List<Url> list = getDataBase().find(Url.class, 0, Integer.MAX_VALUE);
		Map<Long, Url> map = new HashMap<Long, Url>();
		for (Url url : list) {
			map.put(url.getId(), url);
		}
		return (Map<Long, T>) map;
	}

	@Override
	public void store(Long key, T value) {
		Url url = getDataBase().find(Url.class, key);
		if (url == null) {
			getDataBase().persist(value);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void storeAll(Map<Long, T> map) {
		for (Long key : map.keySet()) {
			T url = (T) getDataBase().find(Object.class, key);
			if (url == null) {
				getDataBase().persist(map.get(key));
			}
		}
	}

	@Override
	public void delete(Long key) {
		Url url = getDataBase().find(Url.class, key);
		if (url != null) {
			getDataBase().remove(url);
		}
	}

	@Override
	public void deleteAll(Collection<Long> keys) {
		for (Long key : keys) {
			Url url = getDataBase().find(Url.class, key);
			if (url != null) {
				getDataBase().remove(url);
			}
		}
	}

	private IDataBase getDataBase() {
		if (dataBase == null) {
			dataBase = ApplicationContextManager.getBean(IDataBase.class);
		}
		return dataBase;
	}

}