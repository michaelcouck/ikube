package ikube.cluster;

import ikube.toolkit.ApplicationContextManager;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class ClusterManagerJmsTest {

	@Test
	public void integration() throws Exception {
		ApplicationContextManager.getApplicationContext("/META-INF/common/spring-jms.xml");
		JmsTemplate jmsTemplate = ApplicationContextManager.getBean("jmsTopicTemplate");
		jmsTemplate.send(new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage("Hello World");
			}
		});
		Thread.sleep(1000);
	}

}