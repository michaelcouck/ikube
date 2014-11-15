package ikube.application;

import com.sun.management.GarbageCollectorMXBean;
import ikube.analytics.IAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
@SuppressWarnings({"SpringJavaAutowiredMembersInspection", "unchecked"})
public class GCAnalyzer {

    static final Logger LOGGER = LoggerFactory.getLogger(GCAnalyzer.class);

    static final String EDEN_SPACE = "PS Eden Space"; // New object, and stack
    static final String PERM_GEN = "PS Perm Gen"; // Class loading area
    static final String OLD_GEN = "PS Old Gen"; // Tenured space, permanent

    /**
     * The memory areas that we will monitor.
     */
    static final String[] MEMORY_BLOCKS = {EDEN_SPACE, PERM_GEN, OLD_GEN};

    /**
     * This bean will generate the forecasts from the data collected in the collectors.
     */
    @Autowired
    IAnalyticsService analyticsService;

    /**
     * This object normalizes the snapshots, aggregating them into snapshots of one minute.
     */
    GCSmoother gcSmoother;

    /**
     * A map of JMX connectors keyed by the url built from the address and the port. We keep a reference to
     * these so we can poll them to see if they are still alive, and disconnect from them when the collector is
     * discarded.
     */
    Map<String, JMXConnector> gcConnectorMap;

    /**
     * A map of all the collectors keyed by the url built from the address and the port.
     */
    Map<String, List<GCCollector>> gcCollectorMap;

    GCAnalyzer() {
        gcSmoother = new GCSmoother();
        gcConnectorMap = new HashMap<>();
        gcCollectorMap = new HashMap<>();
    }

    /**
     * This registers the collector, connecting to the remote machine, getting access to various
     * m-beans, {@link java.lang.management.ThreadMXBean}, {@link java.lang.management.OperatingSystemMXBean} and the
     * {@link com.sun.management.GarbageCollectorMXBean}s. A {@link ikube.application.GCCollector} object is instantiated
     * for each garbage collector bean and each memory area. These collectors will then gather data from their respective
     * m-beans, being notified of events and polling for the data, and hold references to snapshots of the collected
     * data.
     *
     * @param address the ip address of the remote jvm to be monitored
     * @param port    the port of the remote machine to access the jndi registry at, i.e. the rmi registry for the m-beans
     */
    public void registerCollector(final String address, final int port) throws Exception {
        // TODO: This should be in a retry block
        String url = buildUri(address, port);
        JMXConnector jmxConnector = getJMXConnector(url);
        MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
        ThreadMXBean threadMXBean = getThreadMXBean(mBeanServerConnection);
        OperatingSystemMXBean operatingSystemMXBean = getOperatingSystemMXBean(mBeanServerConnection);
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = getGarbageCollectorMXBeans(mBeanServerConnection);

        List<GCCollector> gcCollectors = new ArrayList<>();
        for (final GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            ObjectName gcObjectName = garbageCollectorMXBean.getObjectName();
            for (final String memoryBlock : MEMORY_BLOCKS) {
                final GCCollector gcCollector = new GCCollector(memoryBlock, threadMXBean, operatingSystemMXBean, garbageCollectorMXBean);
                class GCCollectorNotificationListener implements NotificationListener {
                    public void handleNotification(final Notification notification, final Object handback) {
                        gcCollector.getGcSnapshot();
                    }
                }
                mBeanServerConnection.addNotificationListener(gcObjectName, new GCCollectorNotificationListener(), null, null);
                gcCollectors.add(gcCollector);
            }
        }

        gcConnectorMap.put(url, jmxConnector);
        gcCollectorMap.put(url, gcCollectors);
    }

    public void unregisterCollector(final String address, final int port) {
        String url = buildUri(address, port);
        gcCollectorMap.remove(url);
        JMXConnector jmxConnector = gcConnectorMap.get(url);
        if (jmxConnector != null) {
            try {
                jmxConnector.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            } finally {
                gcConnectorMap.remove(url);
            }
        }
    }

    /**
     * This method accesses all the data for all the snapshots in all the collectors. Normalizes the data to be
     * consistent over equal time periods using the {@link ikube.application.GCSmoother}. Then creates a matrix of
     * matrices, ...
     *
     * @param address the address of the target jvm get get the garbage collection and system data for
     * @return a vector of matrices of the snapshot data of the target jvm
     */
    public Object[][][] getGcData(final String address, final int port) {
        List<GCCollector> gcCollectors = gcCollectorMap.get(buildUri(address, port));
        if (gcCollectors == null) {
            return null;
        }
        Object[][][] gcTimeSeriesMatrices = new Object[gcCollectors.size()][][];
        for (int i = 0; i < gcCollectors.size(); i++) {
            GCCollector gcCollector = gcCollectors.get(i);
            List<GCSnapshot> smoothedGcSnapshots = gcSmoother.getSmoothedSnapshots(gcCollector.getGcSnapshots());
            Object[][] gcTimeSeriesMatrix = new Object[smoothedGcSnapshots.size()][];
            for (int j = 0; j < smoothedGcSnapshots.size(); j++) {
                GCSnapshot gcSnapshot = smoothedGcSnapshots.get(j);
                Object[] gcTimeSeriesVector = new Object[7];
                gcTimeSeriesVector[0] = new Date(gcSnapshot.start);
                gcTimeSeriesVector[1] = gcSnapshot.delta;
                gcTimeSeriesVector[2] = gcSnapshot.duration;
                gcTimeSeriesVector[3] = gcSnapshot.interval;
                gcTimeSeriesVector[4] = gcSnapshot.perCoreLoad;
                gcTimeSeriesVector[5] = gcSnapshot.runsPerTimeUnit;
                gcTimeSeriesVector[6] = gcSnapshot.usedToMaxRatio;

                gcTimeSeriesMatrix[j] = gcTimeSeriesVector;
            }
            gcTimeSeriesMatrices[i] = gcTimeSeriesMatrix;

            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("Snapshots : " + gcCollector.getGcSnapshots().size());
                LOGGER.error("Smooth snapshots : " + smoothedGcSnapshots.size());
            }
        }
        return gcTimeSeriesMatrices;
    }

    JMXConnector getJMXConnector(final String url) throws IOException {
        JMXServiceURL jmxServiceUrl = new JMXServiceURL(url);
        return JMXConnectorFactory.connect(jmxServiceUrl);
    }

    OperatingSystemMXBean getOperatingSystemMXBean(final MBeanServerConnection mBeanServerConnection) throws MalformedObjectNameException, IOException {
        ObjectInstance osInstance = mBeanServerConnection.queryMBeans(new ObjectName("*:type=OperatingSystem,*"), null).iterator().next();
        return JMX.newMXBeanProxy(mBeanServerConnection, osInstance.getObjectName(), OperatingSystemMXBean.class, false);
    }

    ThreadMXBean getThreadMXBean(final MBeanServerConnection mBeanServerConnection) throws MalformedObjectNameException, IOException {
        ObjectInstance threadInstance = mBeanServerConnection.queryMBeans(new ObjectName("*:type=Threading,*"), null).iterator().next();
        return JMX.newMXBeanProxy(mBeanServerConnection, threadInstance.getObjectName(), ThreadMXBean.class, false);
    }

    List<GarbageCollectorMXBean> getGarbageCollectorMXBeans(final MBeanServerConnection mBeanServerConnection) throws MalformedObjectNameException, IOException {
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = new ArrayList<>();
        Set<ObjectInstance> gcInstances = mBeanServerConnection.queryMBeans(new ObjectName("*:type=GarbageCollector,*"), null);
        for (final ObjectInstance gcInstance : gcInstances) {
            // Create one collector per garbage collector and memory block, so
            // the total collectors will be n(gcs) * m(memory blocks), about 6 then
            ObjectName gcObjectName = gcInstance.getObjectName();
            GarbageCollectorMXBean garbageCollectorMXBean = JMX.newMXBeanProxy(mBeanServerConnection, gcObjectName, GarbageCollectorMXBean.class, true);
            garbageCollectorMXBeans.add(garbageCollectorMXBean);
        }
        return garbageCollectorMXBeans;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    String buildUri(final String address, final int port) {
        // Could we switch to JMXMP, seems nicer!
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("service:jmx:rmi:///jndi/rmi://")
                .append(address)
                .append(":")
                .append(port)
                .append("/jmxrmi");
        return stringBuilder.toString();
    }

    @SuppressWarnings("UnusedDeclaration")
    void printMBeans(final MBeanServerConnection mBeanServerConnection) throws IOException {
        Set<ObjectName> objectNames = mBeanServerConnection.queryNames(null, null);
        for (final ObjectName objectName : objectNames) {
            LOGGER.error("Object name : " + objectName);
        }
    }

}