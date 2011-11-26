package ikube.integration.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.cluster.ClusterManagerJms;
import ikube.cluster.IClusterManager;
import ikube.integration.AbstractIntegration;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class ClusterManagerJmsIntegration extends AbstractIntegration {

	private IClusterManager clusterManager;

	private String lockName = "lockName";
	private String indexName = "indexName";
	private String actionName = "actionName";
	private String indexableName = "indexableName";

	@Before
	public void before() {
		clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
	}

	@Test
	public void startStopWorking() {
		long actionId = clusterManager.startWorking(actionName, indexName, indexableName);
		Server server = clusterManager.getServer();
		logger.info("Action id : " + actionId + ", " + server.getActions().size());
		assertEquals("There should be one action in the server : ", 1, server.getActions().size());
		clusterManager.stopWorking(actionId, actionName, indexName, indexableName);
		server = clusterManager.getServer();
		assertEquals("The action should be deleted : ", 0, server.getActions().size());

		actionId = clusterManager.startWorking(actionName, indexName, indexableName);
		server = clusterManager.getServer();
		logger.info("Action id : " + actionId + ", " + server.getActions().size());
		assertEquals("There should be onr action in the server : ", 1, server.getActions().size());
		
		actionId = clusterManager.startWorking(actionName, indexName, indexableName);
		assertEquals("There should be two actions in the server : ", 2, server.getActions().size());
		
		assertTrue("The server should be working : ", server.getWorking());
	}

	@Test
	public void clusterSynchronisation() throws Exception {
		JmsTemplate jmsTemplate = ApplicationContextManager.getBean(JmsTemplate.class);
		final Serializable serializable = new ClusterManagerJms.Lock(InetAddress.getLocalHost().getHostAddress(),
				System.currentTimeMillis(), Boolean.FALSE);
		jmsTemplate.send(new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createObjectMessage(serializable);
			}
		});

		List<Server> servers = clusterManager.getServers();
		for (Server server : servers) {
			server.getActions().clear();
		}
		boolean anyWorking = clusterManager.anyWorking();
		assertFalse("There should be no servers working : ", anyWorking);
		boolean anyWorkingOnIndex = clusterManager.anyWorking(indexName);
		assertFalse("There should be no servers working on this index : ", anyWorkingOnIndex);

		boolean gotLock = clusterManager.lock(lockName);
		assertTrue("We should have the lock : ", gotLock);

		long actionId = clusterManager.startWorking(actionName, indexName, indexableName);
		anyWorking = clusterManager.anyWorking();
		assertTrue("This server is now working : ", anyWorking);
		anyWorkingOnIndex = clusterManager.anyWorking(indexName);
		assertTrue("This server is working on this index : ", anyWorkingOnIndex);

		clusterManager.stopWorking(actionId, actionName, indexName, indexableName);
		Thread.sleep(1000);
		anyWorking = clusterManager.anyWorking();
		assertFalse("There should be no servers working : ", anyWorking);
		anyWorkingOnIndex = clusterManager.anyWorking(indexName);
		assertFalse("There should be no servers working on this index : ", anyWorkingOnIndex);

		boolean releasedLock = clusterManager.unlock(lockName);
		assertTrue("We should have released the lock : ", releasedLock);

		Server server = clusterManager.getServer();
		assertNotNull("The local server must never be null : ", server);
		servers = clusterManager.getServers();
		assertEquals("There must be at least one server : ", 1, servers.size());
	}

}
