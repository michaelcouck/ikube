package ikube.application;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.toolkit.ThreadUtilities;
import org.junit.Test;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Arrays;
import java.util.List;

import static ikube.toolkit.HttpClientUtilities.doGet;
import static ikube.toolkit.HttpClientUtilities.doPost;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
@SuppressWarnings("FieldCanBeLocal")
public class GCAnalyzerIntegration extends AbstractTest {

    private int jmxPort = 8500;
    private int forecasts = 60;
    private String address = "localhost";

    @Test
    public void integration() throws Exception {
        // Create the collector and register it
        String url = "http://" + address + ":" + SERVER_PORT + "/ikube/service/gc-analyzer/register-collector";
        doPost(url, null,
                new String[]{IConstants.ADDRESS, IConstants.PORT},
                new String[]{address, Integer.toString(jmxPort)}, Boolean.class);

        // Call the garbage collector a couple of times
        gc(30, 10000);

        // Get the data from the collectors
        url = "http://" + address + ":" + SERVER_PORT + "/ikube/service/gc-analyzer/used-to-max-ratio-prediction";
        List list = doGet(url,
                new String[]{IConstants.ADDRESS, IConstants.PORT, IConstants.FORECASTS},
                new String[]{address, Integer.toString(jmxPort), Integer.toString(forecasts)}, List.class);
        logger.error(Arrays.deepToString(list.toArray()));

        // Unregister the collector, terminating the connection
        url = "http://" + address + ":" + SERVER_PORT + "/ikube/service/gc-analyzer/unregister-collector";
        doPost(url, null,
                new String[]{IConstants.ADDRESS, IConstants.PORT},
                new String[]{address, Integer.toString(jmxPort)}, Boolean.class);
    }

    void gc(final int calls, final long sleep) throws IOException {
        GCAnalyzer gcAnalyzer = new GCAnalyzer();
        String url = gcAnalyzer.buildUri(address, jmxPort);
        JMXConnector jmxConnector = gcAnalyzer.getJMXConnector(url);
        MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
        MemoryMXBean memoryMXBean = newPlatformMXBeanProxy(mBeanServerConnection, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
        for (int i = 0; i < calls; i++) {
            memoryMXBean.gc();
            ThreadUtilities.sleep(sleep);
        }
    }

}