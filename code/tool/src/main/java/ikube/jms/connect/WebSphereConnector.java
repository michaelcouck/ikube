package ikube.jms.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * This is the connector for the WebSphere JMS provider. This class will get an {@link InitialContext} to
 * WebSphere, get a reference to the {@link ConnectionFactory} and the default {@link Destination}, then
 * create a {@link JmsTemplate} with the factory and destination, and return the template.
 *
 * @author Michael Couck
 * @version 01.00
 * @see IConnector
 * @since 08-06-2015
 */
public class WebSphereConnector implements IConnector {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * {@inheritDoc}
     */
    @Override
    public JmsTemplate connect(
            final String userid,
            final String password,
            final String url,
            final String connectionFactoryJndiName,
            final String destinationJndiName) throws NamingException {
        InitialContext initialContext = getInitialContext(userid, password, url);
        ConnectionFactory connectionFactory = lookupInJndi(initialContext, connectionFactoryJndiName);
        Destination defaultDestination = (Destination) initialContext.lookup(destinationJndiName);
        logger.info("Destination : {}", defaultDestination);

        UserCredentialsConnectionFactoryAdapter connectionFactoryAdapter = new UserCredentialsConnectionFactoryAdapter();
        connectionFactoryAdapter.setTargetConnectionFactory(connectionFactory);
        connectionFactoryAdapter.setUsername(userid);
        connectionFactoryAdapter.setPassword(password);

        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactoryAdapter);
        jmsTemplate.setDefaultDestination(defaultDestination);

        return jmsTemplate;
    }

    @SuppressWarnings("unchecked")
    <T> T lookupInJndi(final InitialContext initialContext, final String name) throws
            NamingException {
        return (T) initialContext.lookup(name);
    }

    InitialContext getInitialContext(final String userid, final String password, final String url) throws NamingException {
        Hashtable<String, String> properties = new Hashtable<>();
        properties.put(Context.PROVIDER_URL, url);
        properties.put(Context.SECURITY_PRINCIPAL, userid);
        properties.put(Context.SECURITY_CREDENTIALS, password);
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.ibm.websphere.naming.WsnInitialContextFactory");
        logger.debug("Connection properties : url : {}, userid : {}", url, userid);
        return new InitialContext(properties);
    }

}

// This seems to be a way to query the WebSphere queues...
//import com.ibm.websphere.management.AdminClient;
//import com.ibm.websphere.management.AdminClientFactory;
//import com.ibm.websphere.management.exception.ConnectorException;

//try {
//    @SuppressWarnings("UnusedDeclaration")
//    AdminClient adminClient = AdminClientFactory.createAdminClient(null);
//} catch (final ConnectorException e) {
//    throw new RuntimeException(e);
//}