package ikube.cluster.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.model.Server;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Administrator
 * @since 01.10.11
 * @version 01.00
 */
public class CacheInfinispanTest extends ATest {

	private long					id		= 0l;
	private String					name	= IConstants.IKUBE;
	private Server					server	= new Server();

	/** Class under test. */
	private static CacheInfinispan	cacheInfinispan;

	public CacheInfinispanTest() {
		super(CacheInfinispanTest.class);
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		cacheInfinispan = new CacheInfinispan();
		cacheInfinispan.initialise();
	}

	@Before
	public void before() throws Exception {
		Thread.sleep(1000);
	}

	@Test
	public void clear() {
		cacheInfinispan.set(name, id, server);
		int size = cacheInfinispan.size(name);
		assertEquals("There should only be one object in the cache : ", 1, size);
		cacheInfinispan.clear(name);
		size = cacheInfinispan.size(name);
		assertEquals("There should be no objects in the cache : ", 0, size);
	}

	@Test
	public void getStringLong() {
		cacheInfinispan.set(name, id, server);
		Server server = cacheInfinispan.get(name, id);
		assertNotNull("The server is in the cache : ", server);
	}

	@Test
	public void remove() {
		cacheInfinispan.set(name, id, server);
		cacheInfinispan.remove(name, id);
		Server server = cacheInfinispan.get(name, id);
		assertNull("The server should be removed : ", server);
	}

	@Test
	public void lock() {
		boolean locked = cacheInfinispan.lock(IConstants.IKUBE);
		assertTrue(locked);
		boolean unlocked = cacheInfinispan.unlock(IConstants.IKUBE);
		assertTrue(unlocked);
	}

	@Test
	public void getCriteria() {
		int size = 100;
		for (int i = 0; i < size + 5; i++) {
			cacheInfinispan.set(name, id++, new Server());
		}
		List<Server> servers = cacheInfinispan.get(name, null, null, size);
		assertEquals("Should be " + size + " servers : ", size, servers.size());
	}

	@Test(expected = RuntimeException.class)
	public void getStringString() {
		cacheInfinispan.get(IConstants.IKUBE, IConstants.IKUBE);
	}

}
