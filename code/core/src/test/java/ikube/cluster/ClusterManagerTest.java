package ikube.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import ikube.BaseTest;
import ikube.model.IndexableTable;
import ikube.model.Server;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.ThreadUtilities;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
public class ClusterManagerTest extends BaseTest {

	private transient Server remoteServer;

	private transient String indexName;
	private transient String indexableName;

	private transient int batchSize = 10;

	/** The class under test. */
	private IClusterManager clusterManager;

	public ClusterManagerTest() {
		super(ClusterManagerTest.class);
	}

	@Before
	public void before() throws UnknownHostException {
		indexName = indexContext.getIndexName();
		indexableName = ApplicationContextManager.getBeans(IndexableTable.class).values().iterator().next().getName();

		remoteServer = new Server();
		remoteServer.setAddress(InetAddress.getLocalHost().getHostAddress() + ".remote");
		remoteServer.setId(HashUtilities.hash(remoteServer.getAddress()));
		remoteServer.setWorking(Boolean.FALSE);

		clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.clear(Url.class);
		clusterManager.clear(Server.class);
	}

	@After
	public void after() {
		clusterManager.clear(Url.class);
		clusterManager.clear(Server.class);
	}

	@Test
	public void clear() {
		// What we want to achieve here is to add some object to the
		// cache then clear the cache and it should be removed form the map. So
		// we add a server
		List<Server> servers = clusterManager.getServers();
		assertEquals(0, servers.size());
		clusterManager.set(Server.class, remoteServer.getId(), remoteServer);

		// Verify that the server is present in the cache
		servers = clusterManager.getServers();
		assertEquals(1, servers.size());

		// Clear the cache
		clusterManager.clear(Server.class);

		// Verify that the server is no longer in the cache
		servers = clusterManager.getServers();
		assertEquals(0, servers.size());
	}

	@Test
	public void get() {
		// What we want to achieve here is to set an object
		// in the cache then do a sql like query on the cache and
		// we should get the server back again
		String sql = "id = " + this.remoteServer.getId();
		Server server = clusterManager.get(Server.class, sql);
		assertNull(server);

		// Set the server in the cache
		clusterManager.set(Server.class, this.remoteServer.getId(), this.remoteServer);

		// Perform the query and verify that we get the server as a result
		server = clusterManager.get(Server.class, sql);
		assertNotNull(server);

		// Remove the server and verify that there are no results from the query
		clusterManager.clear(Server.class);
		server = clusterManager.get(Server.class, sql);
		assertNull(server);
	}

	@Test
	public void getBatch() throws InterruptedException {
		// int
		List<Url> batch = clusterManager.getBatch(batchSize);
		assertEquals(0, batch.size());

		Url url = new Url();
		url.setId(System.currentTimeMillis());
		clusterManager.set(Url.class, url.getId(), url);

		batch = clusterManager.getBatch(batchSize);
		assertEquals(1, batch.size());

		int iterations = 100;
		for (int i = 0; i < iterations; i++) {
			url = new Url();
			url.setId(System.nanoTime());
			Thread.sleep(0, 1);
			clusterManager.set(Url.class, url.getId(), url);
		}

		batch = clusterManager.getBatch(batchSize);
		assertEquals(batchSize, batch.size());

		batch = clusterManager.getBatch(iterations - batchSize);
		assertEquals(iterations - batchSize, batch.size());

		batch = clusterManager.getBatch(batchSize);
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

		clusterManager.set(Server.class, remoteServer.getId(), remoteServer);

		servers = clusterManager.getServers();
		assertEquals(1, servers.size());

		clusterManager.getServer();

		servers = clusterManager.getServers();
		assertEquals(2, servers.size());
	}

	@Test
	public void set() {
		// Class<T>, Long, T
		Url url = new Url();
		url.setId(System.currentTimeMillis());

		String sql = "id = " + url.getId();
		Url cacheUrl = clusterManager.get(Url.class, sql);
		assertNull(cacheUrl);

		clusterManager.set(Url.class, url.getId(), url);

		cacheUrl = clusterManager.get(Url.class, sql);
		assertNotNull(cacheUrl);
	}

	@Test
	public void setWorking() {
		// First clear the map of servers
		Server localServer = clusterManager.getServer();
		localServer.getActions().clear();
		clusterManager.set(Server.class, localServer.getId(), localServer);
		remoteServer.getActions().clear();
		clusterManager.set(Server.class, remoteServer.getId(), remoteServer);

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
		clusterManager.set(Server.class, remoteServer.getId(), remoteServer);
		// Verify that the remote server has the action and the time we want
		Server server = clusterManager.get(Server.class, "id = " + remoteServer.getId());
		assertNotNull(server);
		assertEquals(1, server.getActions().size());
		assertEquals(expectedStartTime, server.getActions().get(0).getStartTime());

		List<Thread> threads = new ArrayList<Thread>();
		int threadSize = 1;
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
		int size = clusterManager.size(Server.class);
		assertEquals(0, size);

		int iterations = 100;
		for (int i = 0; i < iterations; i++) {
			Url url = new Url();
			url.setId(System.nanoTime());
			Thread.sleep(1);
			clusterManager.set(Url.class, url.getId(), url);
		}

		size = clusterManager.size(Url.class);
		assertEquals(iterations, size);
	}

	public static void main(String[] args) throws Exception {
		ClusterManagerTest clusterManagerTest = new ClusterManagerTest();
		clusterManagerTest.before();
		clusterManagerTest.setWorking();
		clusterManagerTest.after();
	}

}