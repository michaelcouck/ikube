package ikube.integration.cluster;

import ikube.cluster.ClusterManagerJms;
import ikube.toolkit.ApplicationContextManager;

import java.io.Serializable;
import java.net.InetAddress;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class ClusterManagerJmsIntegration {

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
	}

}
