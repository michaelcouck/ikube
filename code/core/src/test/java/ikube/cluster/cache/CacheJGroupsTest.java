package ikube.cluster.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.model.Server;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Administrator
 * @since 01.10.11
 * @version 01.00
 */
public class CacheJGroupsTest extends ATest {

	private long				id		= 0l;
	private String				name	= IConstants.IKUBE;
	private Server				server	= new Server();

	/** Class under test. */
	private static CacheJGroups	cacheJGroups;

	public CacheJGroupsTest() {
		super(CacheJGroupsTest.class);
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		cacheJGroups = new CacheJGroups();
		cacheJGroups.initialise();
	}

	@Test
	public void clear() {
		cacheJGroups.set(name, id, server);
		int size = cacheJGroups.size(name);
		assertEquals("There should only be one object in the cache : ", 1, size);
		cacheJGroups.clear(name);
		size = cacheJGroups.size(name);
		assertEquals("There should be no objects in the cache : ", 0, size);
	}

	@Test
	public void getStringLong() {
		cacheJGroups.set(name, id, server);
		Server server = cacheJGroups.get(name, id);
		assertNotNull("The server is in the cache : ", server);
	}

	@Test
	public void remove() {
		cacheJGroups.set(name, id, server);
		cacheJGroups.remove(name, id);
		Server server = cacheJGroups.get(name, id);
		assertNull("The server should be removed : ", server);
	}

	@Test
	public void lock() {
		boolean locked = cacheJGroups.lock(IConstants.IKUBE);
		assertTrue(locked);
		boolean unlocked = cacheJGroups.unlock(IConstants.IKUBE);
		assertTrue(unlocked);
	}

	@Test
	public void getCriteria() {
		int size = 100;
		for (int i = 0; i < size + 5; i++) {
			cacheJGroups.set(name, id++, new Server());
		}
		List<Server> servers = cacheJGroups.get(name, null, null, size);
		assertEquals("Should be " + size + " servers : ", size, servers.size());
	}

	@Test(expected = RuntimeException.class)
	public void getStringString() {
		cacheJGroups.get(IConstants.IKUBE, IConstants.IKUBE);
	}

}
