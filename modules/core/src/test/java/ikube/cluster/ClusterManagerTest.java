package ikube.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import ikube.BaseTest;
import ikube.action.Index;
import ikube.index.handler.database.IndexableTableHandler;
import ikube.model.Batch;
import ikube.model.Server;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.ThreadUtilities;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClusterManagerTest extends BaseTest {

	private Server serverLocal;
	private Server serverRemote;

	private String indexName;
	private String actionName;
	private String handlerName;

	private int batchSize = 10;

	private IClusterManager clusterManager;

	@Before
	public void before() throws Exception {
		handlerName = IndexableTableHandler.class.getName();
		actionName = Index.class.getName();
		indexName = indexContext.getIndexName();

		serverLocal = new Server();
		serverLocal.getActions().add(serverLocal.new Action(handlerName, actionName, indexName, System.currentTimeMillis()));
		serverLocal.setAddress(InetAddress.getLocalHost().getHostName());
		serverLocal.setId(HashUtilities.hash(serverLocal.getAddress()));
		serverLocal.setWorking(Boolean.FALSE);

		serverRemote = new Server();
		serverRemote.getActions().add(serverRemote.new Action(handlerName, actionName, indexName, System.currentTimeMillis()));
		serverRemote.setAddress(InetAddress.getLocalHost().getHostName() + "serverRemote");
		serverRemote.setId(HashUtilities.hash(serverRemote.getAddress()));
		serverRemote.setWorking(Boolean.FALSE);

		clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
	}

	@After
	public void after() {
		clusterManager.clear(Url.class);
		clusterManager.clear(Batch.class);
		clusterManager.clear(Server.class);
	}

	@Test
	public void clear() {
		// Class<T>
		Set<Server> servers = clusterManager.getServers();
		assertEquals(0, servers.size());

		clusterManager.set(Server.class, serverLocal.getId(), serverLocal);

		servers = clusterManager.getServers();
		assertEquals(1, servers.size());
	}

	@Test
	public void get() {
		// Class<T>, String
		String sql = "id = " + this.serverLocal.getId();
		Server server = clusterManager.get(Server.class, sql);
		assertNull(server);

		clusterManager.set(Server.class, this.serverLocal.getId(), this.serverLocal);

		server = clusterManager.get(Server.class, sql);
		assertNotNull(server);
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
		long idNumber = clusterManager.getIdNumber(indexName, batchSize);
		assertEquals(0, idNumber);

		idNumber = clusterManager.getIdNumber(indexName, batchSize);
		assertEquals(batchSize, idNumber);
	}

	@Test
	public void getServers() {
		Set<Server> servers = clusterManager.getServers();
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
		Server remote = new Server();
		remote.setAddress("remote");
		remote.setWorking(Boolean.TRUE);
		remote.getActions().add(remote.new Action(handlerName, actionName, indexName, startWorkingTime));
		remote.setId(HashUtilities.hash(remote.getAddress()));
		clusterManager.set(Server.class, remote.getId(), remote);

		int threadSize = 1;
		for (int i = 0; i < threadSize; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					for (int i = 0; i < 100; i++) {
						long lastWorkingTime = clusterManager.setWorking(indexName, actionName, handlerName, Boolean.TRUE);
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