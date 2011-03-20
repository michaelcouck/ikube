package ikube.cluster.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import ikube.BaseTest;
import ikube.database.IDataBase;
import ikube.database.odb.DataBaseOdb;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
@Ignore
public class DataBaseOdbMapStoreTest extends BaseTest {

	private transient IDataBase dataBase;
	private transient CacheMapStore cacheMapStore;

	public DataBaseOdbMapStoreTest() {
		super(DataBaseOdbMapStoreTest.class);
	}

	@Before
	public void before() {
		dataBase = ApplicationContextManager.getBean(DataBaseOdb.class);
		delete(dataBase, Url.class);
		cacheMapStore = new CacheMapStore();
	}

	@After
	public void after() {
		delete(dataBase, Url.class);
	}

	@Test
	public void delete() {
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
	public void deleteAll() throws InterruptedException {
		// Collection<Long>
		Map<Long, Object> urls = getUrls(Boolean.TRUE);

		int urlSize = dataBase.find(Url.class, 0, Byte.MAX_VALUE).size();
		assertEquals(urls.size(), urlSize);

		cacheMapStore.deleteAll(urls.keySet());

		urlSize = dataBase.find(Url.class, 0, Byte.MAX_VALUE).size();
		assertEquals(0, urlSize);
	}

	@Test
	public void load() {
		// Long
		Url url = getUrl("localhost");
		url.setId(System.nanoTime());
		dataBase.persist(url);
		Object dataBaseUrl = cacheMapStore.load(url.getId());
		assertNotNull(dataBaseUrl);
	}

	@Test
	public void loadAll() throws InterruptedException {
		// Collection<Long>
		Map<Long, Object> keys = getUrls(Boolean.TRUE);

		Map<Long, Object> map = cacheMapStore.loadAll(keys.keySet());
		assertEquals(keys.size(), map.size());
	}

	@Test
	public void store() {
		// Long, T
		Url url = getUrl("localhost");
		cacheMapStore.store(url.getId(), url);

		Url dataBaseUrl = dataBase.find(Url.class, url.getId());
		assertNotNull(dataBaseUrl);
	}

	@Test
	public void storeAll() throws InterruptedException {
		// Map<Long, T>
		Map<Long, Object> keys = getUrls(Boolean.FALSE);
		cacheMapStore.storeAll(keys);

		List<Url> urls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
		assertEquals(keys.size(), urls.size());
	}

	private Map<Long, Object> getUrls(final boolean persist) throws InterruptedException {
		int iterations = 10;
		Map<Long, Object> map = new HashMap<Long, Object>();
		for (int i = 0; i < iterations; i++) {
			Url url = getUrl("localhost." + i);
			url.setId(System.nanoTime());
			if (persist) {
				dataBase.persist(url);
			}
			map.put(url.getId(), url);
			Thread.sleep(1);
		}
		logger.info("Url size : " + map.size());
		return map;
	}

	private Url getUrl(final String urlString) {
		Url url = new Url();
		url.setUrl(urlString);
		url.setId(HashUtilities.hash(url.getUrl()));
		return url;
	}

}
