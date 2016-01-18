package ikube.jms;

import ikube.AbstractTest;
import ikube.jms.connect.ActiveMQConnector;
import ikube.jms.connect.WebSphereConnector;
import ikube.jms.connect.WeblogicConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

    @BeforeClass
    public static void beforeClass() {
        JmsUtilities.startServer();
    }

    @AfterClass
    public static void afterClass() {
        JmsUtilities.stopServer();
    }

    @Test
    public void publishRemoteWas() throws Exception {
        publisher.publish(
                "admin",
                "password",
                "iiop://192.168.1.102:2809",
                "cell/nodes/app1/servers/OPFClusterApp1/jms/QCF",
                "cell/nodes/app1/servers/OPFClusterApp1/jms/InterchangeLoaderQ",
                "headerName",
                "headerValue",
                "Hello world",
                WebSphereConnector.class.getName(), 1, null);
    }

    @Test
    @Ignore
    public void publishRemoteActiveMQ() throws Exception {
        publisher.publish("userid",
                "password",
                "tcp://localhost:61616",
                "jms/QCF",
                "jms/InterchangeLoaderQ",
                "headerName",
                "headerValue",
                "payload",
                ActiveMQConnector.class.getCanonicalName(), 1, null);
    }

    @Test
    @Ignore
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
                WeblogicConnector.class.getName(), 1, null);
    }

}