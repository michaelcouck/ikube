package ikube.application;

import com.google.gson.reflect.TypeToken;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Analysis;
import ikube.toolkit.ThreadUtilities;
import org.junit.Ignore;
import org.junit.Test;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static ikube.toolkit.HttpClientUtilities.doGet;
import static ikube.toolkit.HttpClientUtilities.doPost;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
@Ignore
@SuppressWarnings("FieldCanBeLocal")
public class GCAnalyzerIntegration extends AbstractTest {

    private int jmxPort = 8500;
    private int serverPort = 9090;
    private int forecasts = 60 * 24;
    private String address = "localhost";

    @Test
    @SuppressWarnings("unchecked")
    public void integration() throws Exception {
        // Create the collector and register it
        String url = "http://" + address + ":" + serverPort + "/ikube/service/gc-analyzer/register-collector";
        doPost(url, null,
                new String[]{IConstants.ADDRESS, IConstants.PORT},
                new String[]{address, Integer.toString(jmxPort)}, Boolean.class);

        // Call the garbage collector a couple of times
        gc(6 * 60, 10000);

        // Get the data from the collectors
        url = "http://" + address + ":" + serverPort + "/ikube/service/gc-analyzer/used-to-max-ratio-prediction";
        Type listType = new TypeToken<List<Analysis>>() {
        }.getType();
        List<Analysis> analyses = doGet(url,
                new String[]{IConstants.ADDRESS, IConstants.PORT, IConstants.FORECASTS},
                new String[]{address, Integer.toString(jmxPort), Integer.toString(forecasts)}, listType);
        // Assert.assertEquals(6, analyses.size());
        for (final Analysis analysis : analyses) {
            ArrayList predictions = (ArrayList) analysis.getOutput();
            for (Object prediction : predictions) {
                for (Object p : (ArrayList) prediction) {
                    for (Object v : (ArrayList) p) {
                        logger.error("V : " + v + ", " + v.getClass());
                    }
                }
            }
        }

        // Unregister the collector, terminating the connection
//        url = "http://" + address + ":" + serverPort + "/ikube/service/gc-analyzer/unregister-collector";
//        doPost(url, null,
//                new String[]{IConstants.ADDRESS, IConstants.PORT},
//                new String[]{address, Integer.toString(jmxPort)}, Boolean.class);
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