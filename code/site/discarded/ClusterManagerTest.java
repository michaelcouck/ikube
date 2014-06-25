package ikube.cluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.action.Index;
import ikube.cluster.cache.ICache;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.Server;
import ikube.toolkit.HashUtilities;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
@Ignore
@SuppressWarnings("deprecation")
public class ClusterManagerTest extends ATest {

	@MockClass(realClass = System.class)
	public static class SystemMock {
		public static int status = -1;

		@Mock()
		public static void exit(int status) {
			// We over ride the system exit!
			SystemMock.status = status;
		}
	}

	private transient Server remoteServer;

	private transient String indexName;
	private transient String indexableName;
	private transient String actionName = Index.class.getSimpleName();

	@SuppressWarnings("unused")
	private transient int batchSize = 10;

	private ICache cacheInfinispan;
	/** The class under test. */
	private ClusterManager clusterManager;

	public ClusterManagerTest() {
		super(ClusterManagerTest.class);
	}

	@Before
	public void before() throws Exception {
		clusterManager = new ClusterManager();
		cacheInfinispan = Mockito.mock(ICache.class);
		Mockit.setUpMocks(SystemMock.class, ApplicationContextManagerMock.class);

		indexName = indexContext.getIndexName();
		indexableName = indexableColumn.getName();

		remoteServer = new Server();
		remoteServer.setAddress(InetAddress.getLocalHost().getHostAddress() + "." + System.nanoTime());
		remoteServer.setId(HashUtilities.hash(remoteServer.getAddress()));

		clusterManager.setCache(cacheInfinispan);
		Server server = new Server();
		Mockito.when(cacheInfinispan.get(anyString(), anyLong())).thenReturn(server);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(SystemMock.class, ApplicationContextManagerMock.class);
	}

	// @Test
	// public void getIdNumber() {
	// // String, long
	// String indexableName = "faq";
	// long startIdNumber = clusterManager.getIdNumber(indexableName, indexName, batchSize, 0);
	// long idNumber = clusterManager.getIdNumber(indexableName, indexName, batchSize, 0);
	// assertEquals(startIdNumber + batchSize, idNumber);
	//
	// long minId = 1234567;
	// startIdNumber = clusterManager.getIdNumber(indexableName, indexName, batchSize, minId);
	// idNumber = clusterManager.getIdNumber(indexableName, indexName, batchSize, minId);
	// assertEquals(minId, startIdNumber);
	// assertEquals(startIdNumber + batchSize, idNumber);
	// }

	// @Test
	// public void setWorking() {
	// // First clear the map of servers
	// Server localServer = clusterManager.getServer();
	// localServer.setAction(null);
	// clusterManager.set(Server.class.getName(), localServer.getId(), localServer);
	// remoteServer.setAction(null);
	// clusterManager.set(Server.class.getName(), remoteServer.getId(), remoteServer);
	//
	// // Verify that there are no actions in any server in the map
	// List<Server> servers = clusterManager.getServers();
	// for (Server server : servers) {
	// assertNull("All the actions should be null : ", server.getAction());
	// }
	//
	// // The local server gets set every time we call the set working
	// // method and the time gets set at that time too
	// final long expectedStartTime = System.currentTimeMillis();
	// localServer.setAction(new Action(0, Index.class.getSimpleName(), indexableName, indexName, new Timestamp(expectedStartTime),
	// Boolean.TRUE));
	// clusterManager.set(Server.class.getName(), localServer.getId(), localServer);
	// // Verify that the remote server has the action and the time we want
	// Server server = clusterManager.get(Server.class.getName(), localServer.getId());
	// assertNotNull(server);
	// assertNotNull("The local server has the action set : ", server.getAction());
	// assertEquals(expectedStartTime, server.getAction().getStartTime().getTime());
	//
	// List<Thread> threads = new ArrayList<Thread>();
	// int threadSize = 3;
	// final int iterations = 100;
	// for (int i = 0; i < threadSize; i++) {
	// Thread thread = new Thread(new Runnable() {
	// public void run() {
	// for (int i = 0; i < iterations; i++) {
	// long actualStartTime = clusterManager.startWorking(Index.class.getSimpleName(), indexName, indexableName);
	// assertTrue("The start time should be incremented each iteration : ", expectedStartTime < actualStartTime);
	// actualStartTime = clusterManager.startWorking(Open.class.getSimpleName(), indexName, indexableName);
	// clusterManager.stopWorking(Index.class.getSimpleName(), indexName, indexableName);
	// clusterManager.stopWorking(Open.class.getSimpleName(), indexName, indexableName);
	// }
	// }
	// }, "ClusterManagerTestThread : " + i);
	// threads.add(thread);
	// thread.start();
	// }
	// ThreadUtilities.waitForThreads(threads);
	// }

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
		// clusterManager.getServer().getAction().setWorking(Boolean.TRUE);
		// clusterManager.getServer().getAction().setIndexName(indexName);
		List<Object> list = new ArrayList<Object>();
		list.add(clusterManager.getServer());
		when(cacheInfinispan.get(Server.class.getName(), null, null, Integer.MAX_VALUE)).thenReturn(list);
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

}