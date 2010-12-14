package ikube.cluster.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import ikube.BaseTest;
import ikube.database.IDataBase;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DataBaseOdbMapStoreTest extends BaseTest {

	private IDataBase dataBase;
	private CacheMapStore cacheMapStore;

	@Before
	public void before() {
		dataBase = ApplicationContextManager.getBean(IDataBase.class);
		delete(dataBase, Url.class);
		cacheMapStore = new CacheMapStore();
	}

	@After
	public void after() {
		delete(dataBase, Url.class);
	}

	@Test
	public void delete() throws Exception {
		// Long
		Url url = getUrl("localhost");
		dataBase.persist(url);
		Url dataBaseUrl = dataBase.find(Url.class, url.getId());
		assertNotNull(dataBaseUrl);

		cacheMapStore.delete(url.getId());

		dataBaseUrl = dataBase.find(Url.class, url.getId());
		assertNull(dataBaseUrl);
	}

	@Test
	public void deleteAll() throws Exception {
		// Collection<Long>
		Map<Long, Object> urls = getUrls(Boolean.TRUE);

		int urlSize = dataBase.find(Url.class, 0, Integer.MAX_VALUE).size();
		assertEquals(urls.size(), urlSize);

		cacheMapStore.deleteAll(urls.keySet());

		urlSize = dataBase.find(Url.class, 0, Integer.MAX_VALUE).size();
		assertEquals(0, urlSize);
	}

	@Test
	public void load() throws Exception {
		// Long
		Url url = getUrl("localhost");
		url.setId(System.nanoTime());
		dataBase.persist(url);
		Object dataBaseUrl = cacheMapStore.load(url.getId());
		assertNotNull(dataBaseUrl);
	}

	@Test
	public void loadAll() throws Exception {
		// Collection<Long>
		Map<Long, Object> keys = getUrls(Boolean.TRUE);

		Map<Long, Object> map = cacheMapStore.loadAll(keys.keySet());
		assertEquals(keys.size(), map.size());
	}

	@Test
	public void store() throws Exception {
		// Long, T
		Url url = getUrl("localhost");
		cacheMapStore.store(url.getId(), url);

		Url dataBaseUrl = dataBase.find(Url.class, url.getId());
		assertNotNull(dataBaseUrl);
	}

	@Test
	public void storeAll() throws Exception {
		// Map<Long, T>
		Map<Long, Object> keys = getUrls(Boolean.FALSE);
		cacheMapStore.storeAll(keys);

		List<Url> urls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
		assertEquals(keys.size(), urls.size());
	}

	private Map<Long, Object> getUrls(boolean persist) throws Exception {
		int iterations = 10;
		Map<Long, Object> map = new HashMap<Long, Object>();
		for (int i = 0; i < iterations; i++) {
			Url url = getUrl("localhost." + i);
			url.setId(System.nanoTime());
			if (persist) {
				dataBase.persist(url);
			}
			map.put(url.getId(), url);
			Thread.sleep(10);
		}
		return map;
	}

	private Url getUrl(String urlString) {
		Url url = new Url();
		url.setUrl(urlString);
		url.setId(HashUtilities.hash(url.getUrl()));
		return url;
	}

}
