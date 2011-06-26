package ikube.cluster.cache;

import ikube.ATest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.toolkit.ApplicationContextManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CacheMapStoreTest extends ATest {

	private CacheMapStore cacheMapStore;
	private Collection<Long> keys;
	private Map<Long, Object> objects;

	public CacheMapStoreTest() {
		super(CacheMapStoreTest.class);
	}

	@Before
	public void before() {
		keys = new ArrayList<Long>();
		keys.add(1l);
		objects = new HashMap<Long, Object>();
		for (Long key : keys) {
			objects.put(key, new Object());
		}
		cacheMapStore = new CacheMapStore();
		Mockit.setUpMocks(ApplicationContextManagerMock.class);
	}
	
	@After
	public void after() {
		Mockit.tearDownMocks(ApplicationContextManager.class);
	}

	@Test
	public void store() {
		// K key, V value
		cacheMapStore.store(1l, new Object());
	}

	@Test
	public void storeAll() {
		// Map<K, V> map
		objects.put(1l, new Object());
		cacheMapStore.storeAll(objects);
	}

	@Test
	public void delete() {
		// K key
		cacheMapStore.delete(1l);
	}

	@Test
	public void deleteAll() {
		// Collection<K> keys
		cacheMapStore.deleteAll(keys);
	}

}
