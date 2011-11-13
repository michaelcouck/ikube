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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Administrator
 * @since 01.10.11
 * @version 01.00
 */
@Ignore
public class CacheInfinispanTest extends ATest {

	private long					id;
	private String					name;
	private Server					server;
	int								iterations	= 1000;

	/** Class under test. */
	private static CacheInfinispan	CACHE_INFINISPAN;

	@BeforeClass
	public static void beforeClass() throws Exception {
		CACHE_INFINISPAN = new CacheInfinispan();
		CACHE_INFINISPAN.setConfigurationFile("META-INF/infinispan.xml");
		CACHE_INFINISPAN.initialise();
	}

	@AfterClass
	public static void afterClass() {
		CACHE_INFINISPAN.destroy();
	}

	public CacheInfinispanTest() {
		super(CacheInfinispanTest.class);
	}

	@Before
	public void before() {
		id = System.currentTimeMillis();
		name = IConstants.IKUBE;
		server = new Server();
		server.setId(id);
	}

	@Test
	public void clear() {
		CACHE_INFINISPAN.set(name, id, server);
		int size = CACHE_INFINISPAN.size(name);
		assertEquals("There should only be one object in the cache : ", 1, size);
		CACHE_INFINISPAN.clear(name);
		size = CACHE_INFINISPAN.size(name);
		assertEquals("There should be no objects in the cache : ", 0, size);
	}

	@Test
	public void getStringLong() {
		CACHE_INFINISPAN.set(name, id, server);
		Server server = CACHE_INFINISPAN.get(name, id);
		assertNotNull("The server is in the cache : ", server);
	}

	@Test
	public void remove() {
		CACHE_INFINISPAN.set(name, id, server);
		CACHE_INFINISPAN.remove(name, id);
		Server server = CACHE_INFINISPAN.get(name, id);
		assertNull("The server should be removed : ", server);
	}

	@Test
	public void lock() {
		boolean locked = CACHE_INFINISPAN.lock(name);
		assertTrue("Cache not locked, should be able to acquire this lock : ", locked);
		locked = CACHE_INFINISPAN.lock(name);
		assertFalse("Cache already locked, shouldn't be able to get the lock : ", locked);

		boolean unlocked = CACHE_INFINISPAN.unlock(name);
		assertTrue("Cache was locked but we should be able to unlock it : ", unlocked);
		unlocked = CACHE_INFINISPAN.unlock(name);
		assertFalse("Cache not locked, so method returns false : ", unlocked);
	}

	@Test
	public void getCriteria() {
		int size = 100;
		for (int i = 0; i < size + 5; i++) {
			CACHE_INFINISPAN.set(name, id++, new Server());
		}
		List<Server> servers = CACHE_INFINISPAN.get(name, null, null, size);
		assertEquals("Should be " + size + " servers : ", size, servers.size());
	}

	@Test(expected = RuntimeException.class)
	public void getStringString() {
		CACHE_INFINISPAN.get(name, name);
	}

	@Test
	public void performanceLockingAndUnlocking() {
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Throwable {
				CACHE_INFINISPAN.lock(name);
				CACHE_INFINISPAN.unlock(name);
			}
		}, "Locking : ", iterations, Boolean.TRUE);
		assertTrue("Must be fast : ", executionsPerSecond > 25);
	}

	@Test
	public void performanceSetting() {
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Throwable {
				CACHE_INFINISPAN.set(server.getClass().getName(), server.getId(), server);
			}
		}, "Setting : ", iterations, Boolean.TRUE);
		assertTrue("Must be fast : ", executionsPerSecond > 25);
	}

	@Test
	public void performanceGetting() {
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Throwable {
				CACHE_INFINISPAN.get(server.getClass().getName(), server.getId());
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
								CACHE_INFINISPAN.get(server.getClass().getName(), server.getId());
							} else if (0.25 < random && random < 0.5) {
								CACHE_INFINISPAN.lock(server.getClass().getName());
							} else if (0.5 < random && random < 0.75) {
								CACHE_INFINISPAN.unlock(server.getClass().getName());
							} else {
								CACHE_INFINISPAN.set(server.getClass().getName(), id, server);
							}
						}
					}, "Multi threaded Infinispan cache : ", iterations, Boolean.TRUE);
				}
			});
			threads.add(thread);
		}
		for (Thread thread : threads) {
			thread.start();
		}
		ThreadUtilities.waitForThreads(threads);
	}

	/**
	 * This main is to start the Infinispan cache in more than one Jvm simulating a cluster.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CacheInfinispan cacheInfinispan = new CacheInfinispan();
		cacheInfinispan.setConfigurationFile("META-INF/infinispan.xml");
		cacheInfinispan.initialise();
		Server server = new Server();
		for (int i = 0; i < 100; i++) {
			sleep();
			server.setId(System.currentTimeMillis());
			cacheInfinispan.set(Server.class.getName(), server.getId(), server);
			sleep();
			List<Server> servers = cacheInfinispan.get(Server.class.getName(), null, null, Integer.MAX_VALUE);
			System.out.println(i + ":" + servers.size() + ":" + (i == servers.size()));
		}
		System.exit(0);
	}

	private static void sleep() throws InterruptedException {
		double sleepTime = 10000;
		long sleep = (long) (Math.random() * sleepTime);
		Thread.sleep(sleep);
	}

}
