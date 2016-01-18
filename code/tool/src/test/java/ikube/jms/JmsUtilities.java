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

    private static BrokerService BROKER = new BrokerService();

    /**
     * This starts a JMS server, using the default(ActiveMQ) JMS provider,
     * so that we can test the JMS functionality in a dynamic way in integration
     * tests.
     */
    public static void startServer() {
        if (BROKER.isStarted()) {
            LOGGER.info("Broker already running : " + BROKER);
            return;
        }
        // Set up the BROKER, i.e. the jms server
        BROKER.setUseJmx(true);
        BROKER.setBrokerName("fred");
        BROKER.setUseShutdownHook(false);

        TransportConnector connector = new TransportConnector();
        try {
            connector.setUri(new URI("tcp://localhost:61616"));
            BROKER.addConnector(connector);
            BROKER.start();
        } catch (final Exception e) {
            LOGGER.error("Exception starting the JMS server programmatically : ", e);
        }
    }

    public static void stopServer() {
        try {
            if (BROKER.isStarted()) {
                BROKER.stop();
            } else {
                LOGGER.info("Broker not running : " + BROKER);
            }
        } catch (final Exception e) {
            LOGGER.error("Exception stopping the JMS server programmatically : ", e);
        }
    }

}
