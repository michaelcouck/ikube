package ikube.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;

import java.net.InetAddress;
import java.util.Map;

import org.jgroups.Message;
import org.junit.Before;
import org.junit.Test;

public class ClusterManagerTest extends BaseTest {

	private String actionName = "actionName";
	private String ipAddress;
	private ClusterManager clusterManager;
	private Server thisServer;
	private Server otherServer;
	private Map<String, Server> servers;

	@Before
	public void before() {
		if (clusterManager == null) {
			try {
				ipAddress = InetAddress.getLocalHost().getHostAddress();
			} catch (Exception e) {
				logger.error("", e);
			}
			clusterManager = ApplicationContextManager.getBean(ClusterManager.class);
			thisServer = getServer(actionName, indexContext.getIndexName(), ipAddress, Boolean.TRUE);
			otherServer = getServer(actionName, indexContext.getIndexName(), "255.255.255.255", Boolean.TRUE);
			servers = clusterManager.getServers(indexContext);
		}
	}

	@Test
	public void anyWorking() {
		// There should be no servers working at all
		boolean anyWorking = clusterManager.anyWorking(indexContext, actionName);
		assertFalse(anyWorking);
		// Set this server working and there are still no servers working on another action
		servers.put(thisServer.getIp(), thisServer);
		anyWorking = clusterManager.anyWorking(indexContext, actionName);
		assertFalse(anyWorking);

		// Set another server working on a different action
		otherServer.setAction(actionName + System.currentTimeMillis());
		servers.put(otherServer.getIp(), otherServer);
		anyWorking = clusterManager.anyWorking(indexContext, actionName);
		assertTrue(anyWorking);

		servers.clear();
	}

	@Test
	public void areWorking() {
		Map<String, Server> servers = clusterManager.getServers(indexContext);

		boolean areWorking = clusterManager.areWorking(indexContext, actionName);
		assertFalse(areWorking);
		// Set this server working
		thisServer.setWorking(Boolean.TRUE);
		servers.put(thisServer.getIp(), thisServer);

		areWorking = clusterManager.areWorking(indexContext, actionName);
		assertTrue(areWorking);

		thisServer.setWorking(Boolean.FALSE);

		// Set another server working
		otherServer.setWorking(Boolean.TRUE);
		otherServer.setAction(actionName);
		servers.put(otherServer.getIp(), otherServer);

		areWorking = clusterManager.areWorking(indexContext, actionName);
		assertTrue(areWorking);

		servers.clear();
	}

	@Test
	public void getLastWorkingTime() {
		// Set both servers and get the last time
		long time = System.currentTimeMillis();
		long plus = 10;
		thisServer.setStart(time);
		otherServer.setStart(time + plus);
		otherServer.setAction(actionName);
		servers.put(thisServer.getIp(), thisServer);
		servers.put(otherServer.getIp(), otherServer);

		long lastWorkingTime = clusterManager.getLastWorkingTime(indexContext, actionName);
		assertEquals(time, lastWorkingTime);

		// Set the action of the other server to something different
		otherServer.setAction(actionName + time);

		lastWorkingTime = clusterManager.getLastWorkingTime(indexContext, actionName);
		assertEquals(-1, lastWorkingTime);

		servers.clear();
	}

	@Test
	public void getNextBatchNumber() {
		// Set this server working
		thisServer.setWorking(Boolean.TRUE);
		servers.put(thisServer.getIp(), thisServer);
		int nextBatchNumber = clusterManager.getNextBatchNumber(indexContext);
		assertEquals(0, nextBatchNumber);

		// Set the other server working with a higher batch number
		otherServer.setAction(actionName);
		otherServer.setBatch(Integer.MAX_VALUE);
		otherServer.setIndex(indexContext.getIndexName());
		otherServer.setStart(System.currentTimeMillis());
		otherServer.setWorking(Boolean.TRUE);
		servers.put(otherServer.getIp(), otherServer);
		nextBatchNumber = clusterManager.getNextBatchNumber(indexContext);
		assertEquals(otherServer.getBatch(), nextBatchNumber);

		servers.clear();
	}

	@Test
	public void isWorking() {
		servers.put(thisServer.getIp(), thisServer);

		thisServer.setWorking(Boolean.FALSE);
		boolean isWorking = clusterManager.isWorking(indexContext);
		assertFalse(isWorking);

		thisServer.setWorking(Boolean.TRUE);
		isWorking = clusterManager.isWorking(indexContext);
		assertTrue(isWorking);

		servers.clear();
	}

	@Test
	public void receive() {
		servers.clear();

		Message message = new Message();
		clusterManager.receive(message);

		Server server = servers.get(thisServer.getIp());
		assertNull(server);

		message.setObject(thisServer);
		clusterManager.receive(message);
		server = servers.get(thisServer.getIp());
		assertNotNull(server);

		servers.clear();
	}

	@Test
	public void resetWorkings() {
		// No servers at all so it will do nothing and return true
		boolean reset = clusterManager.resetWorkings(indexContext, actionName);
		assertTrue(reset);
		// Set a server working
		thisServer.setWorking(Boolean.TRUE);
		servers.put(thisServer.getIp(), thisServer);
		reset = clusterManager.resetWorkings(indexContext, actionName);
		assertFalse(reset);
		thisServer.setWorking(Boolean.FALSE);
		thisServer.setBatch(Integer.MAX_VALUE);
		reset = clusterManager.resetWorkings(indexContext, actionName);
		assertTrue(reset);
		assertEquals(0, thisServer.getBatch());

		servers.clear();
	}

	@Test
	public void setWorking() {
		thisServer.setWorking(Boolean.FALSE);
		servers.put(thisServer.getIp(), thisServer);
		boolean isWorking = clusterManager.isWorking(indexContext);
		assertFalse(isWorking);

		// Set this server working
		thisServer.setWorking(Boolean.TRUE);
		isWorking = clusterManager.isWorking(indexContext);
		assertTrue(isWorking);
		thisServer.setWorking(Boolean.FALSE);

		// Set another server working
		otherServer.setWorking(Boolean.TRUE);
		servers.put(otherServer.getIp(), otherServer);
		isWorking = clusterManager.isWorking(indexContext);
		assertFalse(isWorking);

		servers.clear();
	}

	private Server getServer(String actionName, String indexName, String ipAddress, boolean isWorking) {
		Server server = new Server();
		server.setAction(actionName);
		server.setIndex(indexName);
		server.setIp(ipAddress);
		server.setWorking(isWorking);
		return server;
	}

}
