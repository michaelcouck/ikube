package ikube.jms;

import ikube.AbstractTest;
import ikube.jms.connect.WebSphereConnector;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-01-2015
 */
public class BrowserIntegration extends AbstractTest {

    @Spy
    private Browser browser;

    @Before
    public void before() {
        JmsUtilities.startServer();
    }

    @Test
    public void browseRemoteWas() throws Exception {
        browser.browse(
                "admin",
                "password",
                "iiop://192.168.1.102:2809",
                "cell/nodes/app1/servers/OPFClusterApp1/jms/QCF",
                "cell/nodes/app1/servers/OPFClusterApp1/jms/InterchangeLoaderQ",
                "InterchangeLoaderQ_OPFCluster",
                WebSphereConnector.class.getName());
    }

}