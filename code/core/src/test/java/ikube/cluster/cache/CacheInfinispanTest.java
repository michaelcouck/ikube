package ikube.cluster.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.model.Server;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
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

	private long					id;
	private String					name;
	private Server					server;
	int								iterations	= 100;

	/** Class under test. */
	private static CacheInfinispan	cacheInfinispan;

	public CacheInfinispanTest() {
		super(CacheInfinispanTest.class);
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		cacheInfinispan = new CacheInfinispan();
		cacheInfinispan.setConfigurationFile("META-INF/infinispan.xml");
		cacheInfinispan.initialise();
		// cacheInfinispan = ApplicationContextManager.getBean(CacheInfinispan.class);
	}

	@Before
	public void before() {
		id = 0l;
		name = IConstants.IKUBE;
		server = new Server();
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
		boolean locked = cacheInfinispan.lock(name);
		assertTrue("Cache not locked, should be able to acquire this lock : ", locked);
		locked = cacheInfinispan.lock(name);
		assertFalse("Cache already locked, shouldn't be able to get the lock : ", locked);

		boolean unlocked = cacheInfinispan.unlock(name);
		assertTrue("Cache was locked but we should be able to unlock it : ", unlocked);
		unlocked = cacheInfinispan.unlock(name);
		assertFalse("Cache not locked, so method returns false : ", unlocked);
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
		cacheInfinispan.get(name, name);
	}

	@Test
	public void performanceLockingAndUnlocking() {
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Throwable {
				cacheInfinispan.lock(name);
				cacheInfinispan.unlock(name);
			}
		}, "Locking : ", iterations, Boolean.TRUE);
		assertTrue("Must be fast : ", executionsPerSecond > 25);
	}

	@Test
	public void performanceSetting() {
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Throwable {
				cacheInfinispan.set(server.getClass().getName(), server.getId(), server);
			}
		}, "Setting : ", iterations, Boolean.TRUE);
		assertTrue("Must be fast : ", executionsPerSecond > 25);
	}

	@Test
	public void performanceGetting() {
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Throwable {
				cacheInfinispan.get(server.getClass().getName(), server.getId());
			}
		}, "Getting : ", iterations, Boolean.TRUE);
		assertTrue("Must be fast : ", executionsPerSecond > 25);
	}

	@Test
	public void multiThreaded() {
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < 10; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					PerformanceTester.execute(new PerformanceTester.APerform() {
						@Override
						public void execute() throws Throwable {
							double random = Math.random();
							if (random > 0.25) {
								cacheInfinispan.get(server.getClass().getName(), server.getId());
							} else if (0.25 < random && random < 0.5) {
								cacheInfinispan.lock(server.getClass().getName());
							} else if (0.5 < random && random < 0.75) {
								cacheInfinispan.unlock(server.getClass().getName());
							} else {
								cacheInfinispan.set(server.getClass().getName(), id, server);
							}
						}
					}, "Multi threaded : ", 100000, Boolean.TRUE);
				}
			});
			threads.add(thread);
		}
		for (Thread thread : threads) {
			thread.start();
		}
		ThreadUtilities.waitForThreads(threads);
	}

}
