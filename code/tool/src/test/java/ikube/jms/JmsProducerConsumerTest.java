package ikube.jms;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-03-2015
 */
public class JmsProducerConsumerTest {

    @Before
    public void before() throws Exception {
        JmsUtilities.startServer();
    }

    @Test
    public void main() throws Exception {
        // Connect to the broker with the producer and send some
        // messages, and connect with the consumer and receive some messages
        JmsProducerConsumer.main(null);
        Thread.sleep(60000);
    }

}
