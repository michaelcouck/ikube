package ikube.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.cluster.cache.Cache;
import ikube.listener.ListenerManager;
import ikube.model.Server;
import ikube.model.Url;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.ThreadUtilities;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class ClusterManagerTest extends ATest {

	private transient Server remoteServer;

	private transient String indexName;
	private transient String indexableName;

	private transient int batchSize = 10;

	/** The class under test. */
	private ClusterManager clusterManager;

	public ClusterManagerTest() {
		super(ClusterManagerTest.class);
	}

	@Before
	public void before() throws Exception {
		indexName = INDEX_CONTEXT.getIndexName();
		indexableName = INDEXABLE.getName();

		remoteServer = new Server();
		remoteServer.setAddress(InetAddress.getLocalHost().getHostAddress() + ".remote");
		remoteServer.setId(HashUtilities.hash(remoteServer.getAddress()));
		remoteServer.setWorking(Boolean.FALSE);

		Cache cache = new Cache();
		clusterManager = new ClusterManager(cache);
		cache.initialise();
		clusterManager.clear(Url.class.getName());
		clusterManager.clear(Server.class.getName());
		ListenerManager.removeListeners();
	}

	@After
	public void after() {
		clusterManager.clear(Url.class.getName());
		clusterManager.clear(Server.class.getName());
	}

	@Test
	public void clear() {
		// What we want to achieve here is to add some object to the
		// cache then clear the cache and it should be removed form the map. So
		// we add a server
		List<Server> servers = clusterManager.getServers();
		assertEquals(0, servers.size());
		clusterManager.set(Server.class.getName(), remoteServer.getId(), remoteServer);

		// Verify that the server is present in the cache
		servers = clusterManager.getServers();
		assertEquals(1, servers.size());

		// Clear the cache
		clusterManager.clear(Server.class.getName());

		// Verify that the server is no longer in the cache
		servers = clusterManager.getServers();
		assertEquals(0, servers.size());
	}

	@Test
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
		// First clear the map of servers
		Server localServer = clusterManager.getServer();
		localServer.getActions().clear();
		clusterManager.set(Server.class.getName(), localServer.getId(), localServer);
		remoteServer.getActions().clear();
		clusterManager.set(Server.class.getName(), remoteServer.getId(), remoteServer);

		// Verify that there are no actions in any server in the map
		List<Server> servers = clusterManager.getServers();
		for (Server server : servers) {
			assertEquals(0, server.getActions().size());
		}

		// The local server gets set every time we call the set working
		// method and the time gets set at that time too
		final long expectedStartTime = System.currentTimeMillis();
		// Set a remote server working
		remoteServer.setWorking(Boolean.TRUE);
		remoteServer.getActions().add(remoteServer.new Action(0, indexableName, indexName, expectedStartTime));
		clusterManager.set(Server.class.getName(), remoteServer.getId(), remoteServer);
		// Verify that the remote server has the action and the time we want
		Server server = clusterManager.get(Server.class.getName(), "id = " + remoteServer.getId());
		assertNotNull(server);
		assertEquals(1, server.getActions().size());
		assertEquals(expectedStartTime, server.getActions().get(0).getStartTime());

		List<Thread> threads = new ArrayList<Thread>();
		int threadSize = 3;
		for (int i = 0; i < threadSize; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					for (int i = 0; i < 10; i++) {
						long actualStartTime = clusterManager.setWorking(indexName, indexableName, Boolean.TRUE);
						logger.info("Actual start time : " + actualStartTime + ", expected start time : " + expectedStartTime);
						assertEquals(expectedStartTime, actualStartTime);
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
		clusterManager.setWorking(indexName, indexableName, Boolean.TRUE);
		anyWorking = clusterManager.anyWorking();
		assertTrue("This server should be registered as working : ", anyWorking);
	}

	@Test
	public void anyWorkingIndexName() {
		boolean anyWorking = clusterManager.anyWorking(indexName);
		assertFalse("There should be no servers working : ", anyWorking);
		clusterManager.setWorking(indexName, indexableName, Boolean.TRUE);
		anyWorking = clusterManager.anyWorking(indexName);
		assertTrue("This server should be working on the index now : ", anyWorking);
	}

	@Test
	public void getServer() {
		Server server = clusterManager.getServer();
		assertNotNull("The server can never be null : ", server);
	}

	@Test
	public void isHandled() {
		boolean isHandled = clusterManager.isHandled(indexableName, indexName);
		assertFalse("We haven't handled this indexable : ", isHandled);
		clusterManager.setWorking(indexName, indexableName, Boolean.TRUE);
		isHandled = clusterManager.isHandled(indexableName, indexName);
		assertTrue("This indexable is set to handled by the set working : ", isHandled);
	}

}