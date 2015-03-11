package ikube.jms;

import ikube.AbstractTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-03-2015
 */
public class PublisherIntegration extends AbstractTest {

    @Spy
    private Publisher publisher;

    @Before
    public void before() {
        JmsUtilities.startServer();
    }

    @Test
    public void publish() throws Exception {
        // This just has to pass, we have verified the logic in the unit test
        publisher.publish("userid", "password", "ConnectionFactory", "MyTopic", "jms-client.xml", "property", "value");
    }

}