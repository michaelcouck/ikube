package ikube.jms;

import ikube.jms.connect.Consumer;
import ikube.jms.connect.WebSphereConnector;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Spy;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 01-03-2016
 */
@Ignore
public class ConsumerIntegration extends AbstractIntegration {

    @Spy
    private Consumer consumer;

    @Test
    public void consumeRemoteWas() throws Exception {
        for (final String queue : queues) {
            consumer.consume(
                    userid, // username
                    password, // password
                    url, // url
                    connectionFactory, // connection factory
                    destinationPrefix + queue, // destination name
                    queue + queueSuffix, // queue name
                    connectorType);
        }
    }

}