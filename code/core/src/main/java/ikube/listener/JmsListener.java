package ikube.listener;

import ikube.toolkit.ApplicationContextManager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.network.NetworkBridge;
import org.apache.activemq.network.NetworkConnector;
import org.apache.activemq.xbean.XBeanBrokerService;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * @author Michael Couck
 * @since 05.11.11
 * @version 01.00
 */
public class JmsListener implements IListener {

	private static final Logger LOGGER = Logger.getLogger(JmsListener.class);

	private long id;
	private JmsTemplate jmsTemplate;

	public JmsListener() {
		id = System.nanoTime();
	}

	@Override
	public void handleNotification(Event event) {
		if (!event.getType().equals(Event.JMS)) {
			return;
		}
		try {
			Thread.sleep((long) (Math.random() * 1000l));
		} catch (InterruptedException e) {
		}

		Map<String, Object> beans = ApplicationContextManager.getBeans();
		for (Map.Entry<String, Object> beanDefinition : beans.entrySet()) {
			Object bean = beanDefinition.getValue();
			// LOGGER.error(beanDefinition.getKey() + ":" + bean);
			if (bean == null) {
				continue;
			}
			try {
				if (XBeanBrokerService.class.isAssignableFrom(bean.getClass())) {
					XBeanBrokerService xBeanBrokerService = (XBeanBrokerService) bean;
					Broker broker = xBeanBrokerService.getBroker();
					List<NetworkConnector> networkConnectors = broker.getBrokerService().getNetworkConnectors();
					for (NetworkConnector networkConnector : networkConnectors) {
						Collection<NetworkBridge> networkBridges = networkConnector.activeBridges();
						networkConnector.getDynamicallyIncludedDestinations();
						for (NetworkBridge networkBridge : networkBridges) {
							String remoteAddress = networkBridge.getRemoteAddress();
							LOGGER.error("Remote address : " + remoteAddress);
						}
					}
				}
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}
		jmsTemplate.send(new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createObjectMessage("Message from : " + id);
			}
		});
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

}