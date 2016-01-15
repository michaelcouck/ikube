package ikube.jms.connect;

import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * This is the Weblogic implementation for the connection to the JMS provider.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 08-06-2015
 */
public class WeblogicConnector implements IConnector {

    /**
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
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");

        InitialContext initialContext = new InitialContext(properties);

        ConnectionFactory queueConnectionFactory = (ConnectionFactory) initialContext.lookup(connectionFactory);
        Destination defaultDestination = (Destination) initialContext.lookup(destination);

        JmsTemplate jmsTemplate = new JmsTemplate(queueConnectionFactory);
        jmsTemplate.setDefaultDestination(defaultDestination);
        return jmsTemplate;
    }

}
