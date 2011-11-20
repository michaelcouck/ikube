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

import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class ClusterManagerJmsIntegration extends AbstractIntegration {

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

		String lockName = "lockName";
		String indexName = "indexName";
		String actionName = "actionName";
		String indexableName = "indexableName";

		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.stopWorking(actionName, indexName, indexableName);
		boolean anyWorking = clusterManager.anyWorking();
		assertFalse("There should be no servers working : ", anyWorking);
		boolean anyWorkingOnIndex = clusterManager.anyWorking(indexName);
		assertFalse("There should be no servers working on this index : ", anyWorkingOnIndex);

		boolean gotLock = clusterManager.lock(lockName);
		assertTrue("We should have the lock : ", gotLock);

		clusterManager.startWorking(actionName, indexName, indexableName);
		anyWorking = clusterManager.anyWorking();
		assertTrue("This server is now working : ", anyWorking);
		anyWorkingOnIndex = clusterManager.anyWorking(indexName);
		assertTrue("This server is working on this index : ", anyWorkingOnIndex);

		clusterManager.stopWorking(actionName, indexName, indexableName);
		Thread.sleep(1000);
		anyWorking = clusterManager.anyWorking();
		assertFalse("There should be no servers working : ", anyWorking);
		anyWorkingOnIndex = clusterManager.anyWorking(indexName);
		assertFalse("There should be no servers working on this index : ", anyWorkingOnIndex);

		boolean releasedLock = clusterManager.unlock(lockName);
		assertTrue("We should have released the lock : ", releasedLock);

		Server server = clusterManager.getServer();
		assertNotNull("The local server must never be null : ", server);
		List<Server> servers = clusterManager.getServers();
		assertEquals("There must be at least one server : ", 1, servers.size());
	}

}
