package ikube.jms;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-03-2015
 */
public class JmsUtilities {

    private static Logger LOGGER = LoggerFactory.getLogger(JmsUtilities.class);

    /**
     * This starts a JMS server, using the default(ActiveMQ) JMS provider,
     * so that we can test the JMS functionality in a dynamic way in integration
     * tests.
     */
    public static void startServer() {
        // Set up the broker, i.e. the jms server
        BrokerService broker = new BrokerService();
        broker.setUseJmx(true);
        broker.setBrokerName("fred");
        broker.setUseShutdownHook(false);

        TransportConnector connector = new TransportConnector();
        try {
            connector.setUri(new URI("tcp://localhost:61616"));
            broker.addConnector(connector);
            broker.start();
        } catch (final Exception e) {
            LOGGER.error("Exception starting the JMS server programmatically : ", e);
        }
    }

}
