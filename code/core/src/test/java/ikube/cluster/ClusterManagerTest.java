package ikube.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.action.Index;
import ikube.action.Open;
import ikube.cluster.cache.CacheInfinispan;
import ikube.cluster.cache.CacheJGroups;
import ikube.listener.ListenerManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.Action;
import ikube.model.Server;
import ikube.model.Url;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.ThreadUtilities;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class ClusterManagerTest extends ATest {

	@MockClass(realClass = System.class)
	public static class SystemMock {
		public static int	status	= -1;

		@Mock()
		public static void exit(int status) {
			// We over ride the system exit!
			SystemMock.status = status;
		}
	}

	private transient Server		remoteServer;

	private transient String		indexName;
	private transient String		indexableName;
	private transient String		actionName	= Index.class.getSimpleName();

	private transient int			batchSize	= 10;

	private static CacheInfinispan	CACHE;
	/** The class under test. */
	private ClusterManager			clusterManager;

	public ClusterManagerTest() {
		super(ClusterManagerTest.class);
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		CACHE = new CacheInfinispan();
		CACHE.initialise();
	}

	@Before
	public void before() throws Exception {
		Mockit.setUpMocks(SystemMock.class, ApplicationContextManagerMock.class);

		indexName = INDEX_CONTEXT.getIndexName();
		indexableName = INDEXABLE_COLUMN.getName();

		remoteServer = new Server();
		remoteServer.setAddress(InetAddress.getLocalHost().getHostAddress() + "." + System.nanoTime());
		remoteServer.setId(HashUtilities.hash(remoteServer.getAddress()));
		// remoteServer.setWorking(Boolean.FALSE);

		clusterManager = new ClusterManager(CACHE);
		clusterManager.clear(Url.class.getName());
		clusterManager.clear(Server.class.getName());
		ListenerManager.removeListeners();
	}

	@After
	public void after() {
		clusterManager.clear(Url.class.getName());
		clusterManager.clear(Server.class.getName());
		Mockit.tearDownMocks();
	}

	@Test
	public void clear() {
		// What we want to achieve here is to add some object to the
		// cache then clear the cache and it should be removed form the map. So
		// we add a server
		List<Server> servers = clusterManager.getServers();
		int size = servers.size();
		clusterManager.set(Server.class.getName(), remoteServer.getId(), remoteServer);

		// Verify that the server is present in the cache
		servers = clusterManager.getServers();
		assertEquals(size + 1, servers.size());

		// Clear the cache
		clusterManager.clear(Server.class.getName());

		// Verify that the server is no longer in the cache
		servers = clusterManager.getServers();
		assertEquals(0, servers.size());
	}

	@Test(expected = RuntimeException.class)
	public void getNameSql() {
		// What we want to achieve here is to set an object
		// in the cache then do a sql like query on the cache and
		// we should get the server back again
		String sql = "id = " + this.remoteServer.getId();
		Server server = clusterManager.get(Server.class.getName(), sql);
		assertNull(server);

		// Set the server in the cache
		clusterManager.set(Server.class.getName(), this.remoteServer.getId(), this.remoteServer);

		// Perform the query and verify that we get the server as a result
		server = clusterManager.get(Server.class.getName(), sql);
		assertNotNull(server);

		// Remove the server and verify that there are no results from the query
		clusterManager.clear(Server.class.getName());
		server = clusterManager.get(Server.class.getName(), sql);
		assertNull(server);

		Url url = new Url();
		url.setId(System.currentTimeMillis());

		sql = "id = " + url.getId();
		Url cacheUrl = clusterManager.get(Url.class.getName(), sql);
		assertNull(cacheUrl);

		clusterManager.set(Url.class.getName(), url.getId(), url);

		cacheUrl = clusterManager.get(Url.class.getName(), sql);
		assertNotNull(cacheUrl);
	}

	@Test
	public void getClassNameCriteriaActionSize() throws InterruptedException {
		List<Url> batch = clusterManager.get(Url.class, Url.class.getName(), null, null, batchSize);
		assertEquals(0, batch.size());

		Url url = new Url();
		url.setId(System.currentTimeMillis());
		clusterManager.set(Url.class.getName(), url.getId(), url);

		batch = clusterManager.get(Url.class, Url.class.getName(), null, null, batchSize);
		assertEquals(1, batch.size());

		int iterations = 100;
		for (int i = 0; i < iterations; i++) {
			url = new Url();
			url.setId(System.nanoTime());
			Thread.sleep(0, 1);
			clusterManager.set(Url.class.getName(), url.getId(), url);
		}

		batch = clusterManager.get(Url.class, Url.class.getName(), null, null, batchSize);
		assertEquals(batchSize, batch.size());

		batch = clusterManager.get(Url.class, Url.class.getName(), null, null, iterations - batchSize);
		assertEquals(iterations - batchSize, batch.size());

		batch = clusterManager.get(Url.class, Url.class.getName(), null, null, 0);
		assertEquals(0, batch.size());
	}

	@Test
	public void getIdNumber() {
		// String, long
		String indexableName = "faq";
		long startIdNumber = clusterManager.getIdNumber(indexableName, indexName, batchSize, 0);
		long idNumber = clusterManager.getIdNumber(indexableName, indexName, batchSize, 0);
		assertEquals(startIdNumber + batchSize, idNumber);

		long minId = 1234567;
		startIdNumber = clusterManager.getIdNumber(indexableName, indexName, batchSize, minId);
		idNumber = clusterManager.getIdNumber(indexableName, indexName, batchSize, minId);
		assertEquals(minId, startIdNumber);
		assertEquals(startIdNumber + batchSize, idNumber);
	}

	@Test
	public void getServers() {
		List<Server> servers = clusterManager.getServers();
		assertEquals(0, servers.size());

		clusterManager.set(Server.class.getName(), remoteServer.getId(), remoteServer);

		servers = clusterManager.getServers();
		assertEquals(1, servers.size());

		Server server = clusterManager.getServer();
		clusterManager.set(Server.class.getName(), server.getId(), server);
		servers = clusterManager.getServers();
		assertEquals(2, servers.size());
	}

	@Test
	public void setNameIdObjectGetNameIdRemove() {
		long id = System.currentTimeMillis();
		Url url = new Url();
		url.setId(id);
		clusterManager.set(Url.class.getName(), url.getId(), url);
		url = clusterManager.get(Url.class.getName(), url.getId());
		assertNotNull("We should get the url from the cache : ", url);
		assertEquals("The id should be the url's id : ", (long) id, (long) url.getId());

		clusterManager.remove(Url.class.getName(), url.getId());
		url = clusterManager.get(Url.class.getName(), url.getId());
		assertNull("There should be no url in the cache : ", url);
	}

	@Test
	public void setWorking() {
		ListenerManager.removeListeners();
		// First clear the map of servers
		Server localServer = clusterManager.getServer();
		localServer.setAction(null);
		clusterManager.set(Server.class.getName(), localServer.getId(), localServer);
		remoteServer.setAction(null);
		clusterManager.set(Server.class.getName(), remoteServer.getId(), remoteServer);

		// Verify that there are no actions in any server in the map
		List<Server> servers = clusterManager.getServers();
		for (Server server : servers) {
			assertNull("All the actions should be null : ", server.getAction());
		}

		// The local server gets set every time we call the set working
		// method and the time gets set at that time too
		final long expectedStartTime = System.currentTimeMillis();
		localServer.setAction(new Action(0, Index.class.getSimpleName(), indexableName, indexName, new Timestamp(expectedStartTime), Boolean.TRUE));
		clusterManager.set(Server.class.getName(), localServer.getId(), localServer);
		// Verify that the remote server has the action and the time we want
		Server server = clusterManager.get(Server.class.getName(), localServer.getId());
		assertNotNull(server);
		assertNotNull("The local server has the action set : ", server.getAction());
		assertEquals(expectedStartTime, server.getAction().getStartTime().getTime());

		List<Thread> threads = new ArrayList<Thread>();
		int threadSize = 3;
		final int iterations = 100;
		for (int i = 0; i < threadSize; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					for (int i = 0; i < iterations; i++) {
						long actualStartTime = clusterManager.startWorking(Index.class.getSimpleName(), indexName, indexableName);
						assertTrue("The start time should be incremented each iteration : ", expectedStartTime < actualStartTime);
						actualStartTime = clusterManager.startWorking(Open.class.getSimpleName(), indexName, indexableName);
						clusterManager.stopWorking(Index.class.getSimpleName(), indexName, indexableName);
						clusterManager.stopWorking(Open.class.getSimpleName(), indexName, indexableName);
					}
				}
			}, "ClusterManagerTestThread : " + i);
			threads.add(thread);
			thread.start();
		}
		ThreadUtilities.waitForThreads(threads);
	}

	@Test
	public void size() throws InterruptedException {
		// Class<T>
		int size = clusterManager.size(Server.class.getName());
		assertEquals(0, size);

		int iterations = 100;
		for (int i = 0; i < iterations; i++) {
			Url url = new Url();
			url.setId(System.nanoTime());
			Thread.sleep(1);
			clusterManager.set(Url.class.getName(), url.getId(), url);
		}

		size = clusterManager.size(Url.class.getName());
		assertEquals(iterations, size);
	}

	@Test
	public void anyWorking() {
		boolean anyWorking = clusterManager.anyWorking();
		assertFalse("We haven't set any server working : ", anyWorking);
		clusterManager.startWorking(actionName, indexName, indexableName);
		anyWorking = clusterManager.anyWorking();
		assertFalse("This server should not be registered as working : ", anyWorking);
	}

	@Test
	public void anyWorkingIndexName() {
		boolean anyWorking = clusterManager.anyWorking(indexName);
		assertFalse("There should be no servers working : ", anyWorking);
		clusterManager.startWorking(actionName, indexName, indexableName);
		anyWorking = clusterManager.anyWorking(indexName);
		assertTrue("This server should be working on the index now : ", anyWorking);
	}

	@Test
	public void getServer() {
		Server server = clusterManager.getServer();
		assertNotNull("The server can never be null : ", server);
	}

	@Test
	public void removeDeadServer() {
		// TODO implement me
	}

	/**
	 * To test the configuration you have to start this test twice and verify that there are messages passed between the
	 * Jvms.
	 */
	@Test
	@Ignore
	public void jGroupsConfiguration() throws Exception {
		CacheJGroups cacheJGroups = new CacheJGroups();
		cacheJGroups.initialise();
		cacheJGroups.set(Server.class.getName(), System.currentTimeMillis(), new Server());
		Thread.sleep(10000);
		List<Server> servers = cacheJGroups.get(Server.class.getName(), null, null, Integer.MAX_VALUE);
		logger.error("Servers : " + servers.size());
	}

}