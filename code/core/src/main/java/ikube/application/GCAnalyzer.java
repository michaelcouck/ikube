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

    @Autowired
    private IAnalyticsService analyticsService;

    private Map<String, List<GCCollector>> gcCollectorMap = new HashMap<>();

    public void registerCollector(final String address, final int port) throws Exception {
        // TODO: This should be in a retry block
        MBeanServerConnection mBeanServerConnection = getMBeanServerConnection(address, port);
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

        gcCollectorMap.put(address, gcCollectors);
    }

    MBeanServerConnection getMBeanServerConnection(final String address, final int port) throws IOException {
        String url = buildUri(address, port);
        JMXServiceURL jmxServiceUrl = new JMXServiceURL(url);
        JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceUrl);
        return jmxConnector.getMBeanServerConnection();
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

    public void unregisterCollector(final String address) {
    }

    public Object[][][] getGcData(final String address) {
        List<GCCollector> gcCollectors = gcCollectorMap.get(address);
        if (gcCollectors == null) {
            return null;
        }
        Object[][][] gcTimeSeriesMatrices = new Object[gcCollectors.size()][][];
        for (int i = 0; i < gcCollectors.size(); i++) {
            GCCollector gcCollector = gcCollectors.get(i);
            GCSmoother gcSmoother = new GCSmoother();
            List<GCSnapshot> smoothedGcSnapshots = gcSmoother.getSmoothedSnapshots(gcCollector.getGcSnapshots());

            LOGGER.debug("Snapshots : " + gcCollector.getGcSnapshots().size());
            LOGGER.debug("Smooth snapshots : " + smoothedGcSnapshots.size());

            Object[][] gcTimeSeriesMatrix = new Object[smoothedGcSnapshots.size()][];
            for (int j = 0; j < smoothedGcSnapshots.size(); j++) {
                GCSnapshot gcSnapshot = smoothedGcSnapshots.get(j);
                Object[] gcTimeSeriesVector = new Object[7];
                gcTimeSeriesVector[0] = gcSnapshot.delta;
                gcTimeSeriesVector[1] = gcSnapshot.duration;
                gcTimeSeriesVector[2] = gcSnapshot.interval;
                gcTimeSeriesVector[3] = gcSnapshot.perCoreLoad;
                gcTimeSeriesVector[4] = gcSnapshot.runsPerTimeUnit;
                gcTimeSeriesVector[5] = gcSnapshot.usedToMaxRatio;
                gcTimeSeriesVector[6] = new Date(gcSnapshot.start);
                gcTimeSeriesMatrix[j] = gcTimeSeriesVector;
            }
            gcTimeSeriesMatrices[i] = gcTimeSeriesMatrix;
        }
        return gcTimeSeriesMatrices;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private String buildUri(final String address, final int port) {
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
    private void printMBeans(final MBeanServerConnection mBeanServerConnection) throws IOException {
        Set<ObjectName> objectNames = mBeanServerConnection.queryNames(null, null);
        for (final ObjectName objectName : objectNames) {
            LOGGER.error("Object name : " + objectName);
        }
    }

}