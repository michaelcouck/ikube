package ikube.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import ikube.BaseTest;
import ikube.model.Server;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.ThreadUtilities;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClusterManagerTest extends BaseTest {

	private Server serverLocal;
	private Server serverRemote;

	private String indexName;
	private String indexableName;

	private int batchSize = 10;

	/** The class under test. */
	private IClusterManager clusterManager;

	@Before
	public void before() throws Exception {
		indexName = indexContext.getIndexName();
		indexableName = "faq";

		serverLocal = new Server();
		serverLocal.setAddress(InetAddress.getLocalHost().getHostName());
		serverLocal.setId(HashUtilities.hash(serverLocal.getAddress()));
		serverLocal.setWorking(Boolean.FALSE);

		serverRemote = new Server();
		serverRemote.setAddress(InetAddress.getLocalHost().getHostName() + "serverRemote");
		serverRemote.setId(HashUtilities.hash(serverRemote.getAddress()));
		serverRemote.setWorking(Boolean.FALSE);

		clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
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
		clusterManager.set(Server.class, serverLocal.getId(), serverLocal);

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
		String sql = "id = " + this.serverLocal.getId();
		Server server = clusterManager.get(Server.class, sql);
		assertNull(server);

		// Set the server in the cache
		clusterManager.set(Server.class, this.serverLocal.getId(), this.serverLocal);

		// Perform the query and verify that we get the server as a result
		server = clusterManager.get(Server.class, sql);
		assertNotNull(server);

		// Remove the server and verify that there are no results from the query
		clusterManager.clear(Server.class);
		server = clusterManager.get(Server.class, sql);
		assertNull(server);
	}

	@Test
	public void getBatch() throws Exception {
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
			Thread.sleep(1);
			clusterManager.set(Url.class, url.getId(), url);
		}

		batch = clusterManager.getBatch(batchSize);
		assertEquals(batchSize, batch.size());
	}

	@Test
	public void getIdNumber() {
		// String, long
		String indexableName = "faq";
		long startIdNumber = clusterManager.getIdNumber(indexableName, indexName, batchSize);
		long idNumber = clusterManager.getIdNumber(indexableName, indexName, batchSize);
		assertEquals(startIdNumber + batchSize, idNumber);
	}

	@Test
	public void getServers() {
		List<Server> servers = clusterManager.getServers();
		assertEquals(0, servers.size());

		clusterManager.set(Server.class, serverLocal.getId(), serverLocal);

		servers = clusterManager.getServers();
		assertEquals(1, servers.size());

		clusterManager.set(Server.class, serverRemote.getId(), serverRemote);

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
		// String, String, String, boolean
		List<Thread> threads = new ArrayList<Thread>();
		// Set a remote server working
		final long startWorkingTime = System.currentTimeMillis();
		serverRemote.setWorking(Boolean.TRUE);
		serverRemote.getActions().add(serverRemote.new Action(0, indexableName, indexName, startWorkingTime));
		clusterManager.set(Server.class, serverRemote.getId(), serverRemote);

		int threadSize = 3;
		for (int i = 0; i < threadSize; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					for (int i = 0; i < 10; i++) {
						long lastWorkingTime = clusterManager.setWorking(indexName, indexableName, Boolean.TRUE);
						logger.info("Last working time : " + lastWorkingTime + ", " + startWorkingTime);
						assertEquals(startWorkingTime, lastWorkingTime);
					}
				}
			}, "ClusterManagerTestThread : " + i);
			threads.add(thread);
			thread.start();
		}

		ThreadUtilities.waitForThreads(threads);
	}

	@Test
	public void size() throws Exception {
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