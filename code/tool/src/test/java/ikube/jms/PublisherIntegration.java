package ikube.jms;

import ikube.AbstractTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Spy;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-03-2015
 */
@Ignore
public class PublisherIntegration extends AbstractTest {

    @Spy
    private Publisher publisher;

    @Before
    public void before() {
        JmsUtilities.startServer();
    }

    @Test
    public void publishRemoteWebLogic() throws Exception {
        publisher.publish(
                "qcfuser",
                "passw0rd",
                "t3://be-qa-cs-18.clear2pay.com:8001",
                "jms/QCF",
                "jms/InterchangeLoaderQ",
                "headerName",
                "headerValue",
                "payload",
                "ikube.jms.connection.Weblogic");
    }

    @Test
    public void publishRemoteMQ() throws Exception {
        publisher.publish(
                "qcfuser",
                "passw0rd",
                "iiop:ikube.be:2809",
                "jms/QCF",
                "jms/InterchangeLoaderQ",
                "headerName",
                "headerValue",
                "payload",
                "ikube.jms.connection.MQ");
    }

    @Test
    public void publishRemoteActiveMQ() throws Exception {
        publisher.publish("userid",
                "password",
                "tcp://localhost:61616",
                "jms/QCF",
                "jms/InterchangeLoaderQ",
                "headerName",
                "headerValue",
                "payload",
                "ikube.jms.connection.ActiveMQ");
    }

}