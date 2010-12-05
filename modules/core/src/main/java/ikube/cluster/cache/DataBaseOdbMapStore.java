package ikube.cluster.cache;

import ikube.database.IDataBase;
import ikube.model.Url;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hazelcast.core.MapStore;

public class DataBaseOdbMapStore implements MapStore<Long, Url> {

	private IDataBase dataBase;

	@Override
	public Url load(Long key) {
		return dataBase.find(Url.class, key);
	}

	@Override
	public Map<Long, Url> loadAll(Collection<Long> keys) {
		List<Url> list = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
		Map<Long, Url> map = new HashMap<Long, Url>();
		for (Url url : list) {
			map.put(url.getHash(), url);
		}
		return map;
	}

	@Override
	public void store(Long key, Url value) {
		Url url = dataBase.find(Url.class, key);
		if (url == null) {
			dataBase.persist(value);
		}
	}

	@Override
	public void storeAll(Map<Long, Url> map) {
		for (Long key : map.keySet()) {
			Url url = dataBase.find(Url.class, key);
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
