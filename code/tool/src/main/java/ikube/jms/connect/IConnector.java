package ikube.jms.connect;

import org.springframework.jms.core.JmsTemplate;

import javax.naming.NamingException;

/**
 * This interface encapsulates the connection to various providers, like SIBus, Weblogic and MQ. The
 * result of the operation is a JMS ({@link org.springframework.jms.core.JmsTemplate}) template from Spring populated
 * and connected to the target connection factory.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 08-06-2015
 */
public interface IConnector {

    /**
     * This method connects to the specified(sub class implementation) JMS provider be that MQ or ActiveMQ. The
     * JMS template is populated with the {@link javax.jms.ConnectionFactory}
     *
     * @param userid            the user name to use to connect to the destinations
     * @param password          the password for the same above
     * @param url               the url for the provider
     * @param connectionFactory the session factory name in JNDI
     * @param destination       the destination name for the queue/topic in JNDI
     * @return the jms template populated with a connection factory and the default destination
     */
    JmsTemplate connect(final String userid, final String password, final String url, final String connectionFactory, final String destination) throws NamingException;

}
