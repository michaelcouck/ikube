package ikube.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.action.Index;
import ikube.index.handler.database.IndexableTableHandler;
import ikube.model.Batch;
import ikube.model.Server;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.HashUtilities;

import java.net.InetAddress;
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
		serverLocal.setAction(serverLocal.new Action(handlerName, actionName, indexName, System.currentTimeMillis()));
		serverLocal.setAddress(InetAddress.getLocalHost().getHostName());
		serverLocal.setId(HashUtilities.hash(serverLocal.getAddress()));
		serverLocal.setWorking(Boolean.FALSE);

		serverRemote = new Server();
		serverRemote.setAction(serverRemote.new Action(handlerName, actionName, indexName, System.currentTimeMillis()));
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
	public void anyWorking() throws Exception {
		// String
		boolean anyWorking = clusterManager.anyWorking(Index.class.getName());
		assertFalse(anyWorking);

		clusterManager.set(Server.class, serverLocal.getId(), serverLocal);

		anyWorking = clusterManager.anyWorking(Index.class.getName());
		assertFalse(anyWorking);

		serverRemote.setWorking(Boolean.TRUE);
		clusterManager.set(Server.class, serverRemote.getId(), serverRemote);

		anyWorking = clusterManager.anyWorking(Index.class.getName());
		assertTrue(anyWorking);
	}

	@Test
	public void anyWorkingOnIndex() {
		// String
		boolean anyWorkingOnIndex = clusterManager.anyWorkingOnIndex(indexContext.getIndexName());
		assertFalse(anyWorkingOnIndex);

		serverLocal.setWorking(Boolean.TRUE);
		clusterManager.set(Server.class, serverLocal.getId(), serverLocal);

		anyWorkingOnIndex = clusterManager.anyWorkingOnIndex(indexContext.getIndexName());
		assertTrue(anyWorkingOnIndex);
	}

	@Test
	public void areWorking() {
		// String, String
		boolean areWorking = clusterManager.areWorking(indexName, actionName);
		assertFalse(areWorking);

		serverRemote.setWorking(Boolean.TRUE);
		clusterManager.set(Server.class, serverRemote.getId(), serverRemote);

		areWorking = clusterManager.areWorking(indexName, actionName);
		assertTrue(areWorking);
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
		Server cacheServer = clusterManager.getServer();
		assertFalse(cacheServer.isWorking());

		long lastWorkingTime = clusterManager.setWorking(indexName, actionName, handlerName, Boolean.FALSE);

		cacheServer = clusterManager.getServer();
		assertFalse(cacheServer.isWorking());

		lastWorkingTime = clusterManager.setWorking(indexName, actionName, handlerName, Boolean.TRUE);

		cacheServer = clusterManager.getServer();
		assertTrue(cacheServer.isWorking());
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

}