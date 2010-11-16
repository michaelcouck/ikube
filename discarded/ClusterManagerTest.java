package ikube.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.model.Token;
import ikube.toolkit.ApplicationContextManager;

import java.net.InetAddress;
import java.util.Map;

import org.jgroups.Message;
import org.junit.Before;
import org.junit.Test;

public class ClusterManagerTest extends BaseTest {

	private String actionName = "actionName";
	private String ipAddress;
	// private ClusterManager clusterManager;
	private Token thisServer;
	private Token otherServer;
	private Map<String, Token> tokens;

//	@Before
//	public void before() {
//		if (clusterManager == null) {
//			try {
//				ipAddress = InetAddress.getLocalHost().getHostAddress();
//			} catch (Exception e) {
//				logger.error("", e);
//			}
//			clusterManager = ApplicationContextManager.getBean(ClusterManager.class);
//			thisServer = getServer(actionName, indexContext.getIndexName(), ipAddress, Boolean.TRUE);
//			otherServer = getServer(actionName, indexContext.getIndexName(), "255.255.255.255", Boolean.TRUE);
//			tokens = clusterManager.getServers(indexContext);
//		}
//	}

//	@Test
//	public void anyWorking() {
//		// There should be no servers working at all
//		boolean anyWorking = clusterManager.anyWorking(indexContext, actionName);
//		assertFalse(anyWorking);
//		// Set this server working and there are still no servers working on another action
//		tokens.put(thisServer.getIp(), thisServer);
//		anyWorking = clusterManager.anyWorking(indexContext, actionName);
//		assertFalse(anyWorking);
//
//		// Set another server working on a different action
//		otherServer.setAction(actionName + System.currentTimeMillis());
//		tokens.put(otherServer.getIp(), otherServer);
//		anyWorking = clusterManager.anyWorking(indexContext, actionName);
//		assertTrue(anyWorking);
//
//		tokens.clear();
//	}
//
//	@Test
//	public void areWorking() {
//		Map<String, Token> tokens = clusterManager.getServers(indexContext);
//
//		boolean areWorking = clusterManager.areWorking(indexContext, actionName);
//		assertFalse(areWorking);
//		// Set this server working
//		thisServer.setWorking(Boolean.TRUE);
//		tokens.put(thisServer.getIp(), thisServer);
//
//		areWorking = clusterManager.areWorking(indexContext, actionName);
//		assertTrue(areWorking);
//
//		thisServer.setWorking(Boolean.FALSE);
//
//		// Set another server working
//		otherServer.setWorking(Boolean.TRUE);
//		otherServer.setAction(actionName);
//		tokens.put(otherServer.getIp(), otherServer);
//
//		areWorking = clusterManager.areWorking(indexContext, actionName);
//		assertTrue(areWorking);
//
//		tokens.clear();
//	}
//
//	@Test
//	public void getLastWorkingTime() {
//		// Set both servers and get the last time
//		long time = System.currentTimeMillis();
//		long plus = 10;
//		thisServer.setStart(time);
//		otherServer.setStart(time + plus);
//		otherServer.setAction(actionName);
//		tokens.put(thisServer.getIp(), thisServer);
//		tokens.put(otherServer.getIp(), otherServer);
//
//		long lastWorkingTime = clusterManager.getLastWorkingTime(indexContext, actionName);
//		assertEquals(time, lastWorkingTime);
//
//		// Set the action of the other server to something different
//		otherServer.setAction(actionName + time);
//
//		lastWorkingTime = clusterManager.getLastWorkingTime(indexContext, actionName);
//		assertEquals(-1, lastWorkingTime);
//
//		tokens.clear();
//	}
//
//	@Test
//	public void getNextBatchNumber() {
//		// Set this server working
//		thisServer.setWorking(Boolean.TRUE);
//		tokens.put(thisServer.getIp(), thisServer);
//		int nextBatchNumber = clusterManager.getNextBatchNumber(indexContext);
//		assertEquals(0, nextBatchNumber);
//
//		// Set the other server working with a higher batch number
//		otherServer.setAction(actionName);
//		otherServer.setBatch(Integer.MAX_VALUE);
//		otherServer.setIndex(indexContext.getIndexName());
//		otherServer.setStart(System.currentTimeMillis());
//		otherServer.setWorking(Boolean.TRUE);
//		tokens.put(otherServer.getIp(), otherServer);
//		nextBatchNumber = clusterManager.getNextBatchNumber(indexContext);
//		assertEquals(otherServer.getBatch(), nextBatchNumber);
//
//		tokens.clear();
//	}
//
//	@Test
//	public void isWorking() {
//		tokens.put(thisServer.getIp(), thisServer);
//
//		thisServer.setWorking(Boolean.FALSE);
//		boolean isWorking = clusterManager.isWorking(indexContext);
//		assertFalse(isWorking);
//
//		thisServer.setWorking(Boolean.TRUE);
//		isWorking = clusterManager.isWorking(indexContext);
//		assertTrue(isWorking);
//
//		tokens.clear();
//	}
//
//	@Test
//	public void receive() {
//		tokens.clear();
//
//		Message message = new Message();
//		clusterManager.receive(message);
//
//		Token token = tokens.get(thisServer.getIp());
//		assertNull(token);
//
//		message.setObject(thisServer);
//		clusterManager.receive(message);
//		token = tokens.get(thisServer.getIp());
//		assertNotNull(token);
//
//		tokens.clear();
//	}
//
//	@Test
//	public void resetWorkings() {
//		// No servers at all so it will do nothing and return true
//		boolean reset = clusterManager.resetWorkings(indexContext, actionName);
//		assertTrue(reset);
//		// Set a server working
//		thisServer.setWorking(Boolean.TRUE);
//		tokens.put(thisServer.getIp(), thisServer);
//		reset = clusterManager.resetWorkings(indexContext, actionName);
//		assertFalse(reset);
//		thisServer.setWorking(Boolean.FALSE);
//		thisServer.setBatch(Integer.MAX_VALUE);
//		reset = clusterManager.resetWorkings(indexContext, actionName);
//		assertTrue(reset);
//		assertEquals(0, thisServer.getBatch());
//
//		tokens.clear();
//	}
//
//	@Test
//	public void setWorking() {
//		thisServer.setWorking(Boolean.FALSE);
//		tokens.put(thisServer.getIp(), thisServer);
//		boolean isWorking = clusterManager.isWorking(indexContext);
//		assertFalse(isWorking);
//
//		// Set this server working
//		thisServer.setWorking(Boolean.TRUE);
//		isWorking = clusterManager.isWorking(indexContext);
//		assertTrue(isWorking);
//		thisServer.setWorking(Boolean.FALSE);
//
//		// Set another server working
//		otherServer.setWorking(Boolean.TRUE);
//		tokens.put(otherServer.getIp(), otherServer);
//		isWorking = clusterManager.isWorking(indexContext);
//		assertFalse(isWorking);
//
//		tokens.clear();
//	}
//
//	private Token getServer(String actionName, String indexName, String ipAddress, boolean isWorking) {
//		Token token = new Token();
//		token.setAction(actionName);
//		token.setIndex(indexName);
//		token.setIp(ipAddress);
//		token.setWorking(isWorking);
//		return token;
//	}

}
