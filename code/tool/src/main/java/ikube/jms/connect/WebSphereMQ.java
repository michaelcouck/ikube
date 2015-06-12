package ikube.jms.connect;

import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * NOTE: Not fully implemented/tested.
 * <p/>
 * This is the MQ implementation for the connection to the JMS provider.
 *
 * @author Michael Couck
 * @version 01.00
 * @see IConnector
 * @since 08-06-2015
 */
public class WebSphereMQ implements IConnector {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * NOT FULLY IMPLEMENTED: 08-06-2015
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public JmsTemplate connect(
            final String userid,
            final String password,
            final String url,
            final String connectionFactory,
            final String destination) throws NamingException {
        Hashtable<String, String> properties = new Hashtable<>();
        properties.put(Context.PROVIDER_URL, url);
        properties.put(Context.SECURITY_PRINCIPAL, userid);
        properties.put(Context.SECURITY_CREDENTIALS, password);
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.ibm.websphere.naming.WsnInitialContextFactory");

        InitialContext initialContext = new InitialContext(properties);


        ConnectionFactory queueConnectionFactory = (ConnectionFactory) initialContext.lookup(connectionFactory);
        Destination defaultDestination = (Destination) initialContext.lookup(destination);

        JmsTemplate jmsTemplate = new JmsTemplate(queueConnectionFactory);
        jmsTemplate.setDefaultDestination(defaultDestination);
        return jmsTemplate;
    }

    /**
     * NOT FULLY IMPLEMENTED: 08-06-2015
     */
    @SuppressWarnings("UnusedDeclaration")
    private void url(final String userid, final String password, final String url, final String connectionFactory, final int port) throws JMSException {
        MQQueueConnectionFactory queueConnectionFactory = new MQQueueConnectionFactory();

        queueConnectionFactory.setPort(port); // default is 1414
        queueConnectionFactory.setHostName(url); // localhost
        queueConnectionFactory.setLocalAddress("localhost");
        queueConnectionFactory.setQueueManager(connectionFactory); // QM_thinkpad, QCF, BPECF
        queueConnectionFactory.setChannel("SYSTEM.DEF.SVRCONN");

        queueConnectionFactory.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);

        try {
            Connection connection = queueConnectionFactory.createConnection(userid, password);
            @SuppressWarnings("UnusedDeclaration")
            Session session = connection.createSession(Boolean.FALSE, TopicSession.AUTO_ACKNOWLEDGE);
        } catch (final JMSException e) {
            logger.error("Error code : " + e.getErrorCode());
            logger.error("Top level exception : ", e);
            logger.error("Linked exception : ", e.getLinkedException());
            for (final Throwable supressed : e.getSuppressed()) {
                logger.error("Suppressed exception : ", supressed);
            }
        }

        JmsTemplate jmsTemplate = new JmsTemplate(queueConnectionFactory);
        Destination destination = jmsTemplate.getDefaultDestination();
        jmsTemplate.send(destination, new MessageCreator() {
            @Override
            public Message createMessage(final Session session) throws JMSException {
                TextMessage textMessage = session.createTextMessage();
                textMessage.setText("Hello World!");
                return textMessage;
            }
        });
    }

}
