package ikube.application;

import ikube.analytics.IAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
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

    @Autowired
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    private IAnalyticsService analyticsService;

    private Map<String, GCCollector> gcCollectorMap = new HashMap<>();

    public void registerCollector(final String address, final int port) throws IOException, MalformedObjectNameException {
        String url = buildUri(address, port);

        JMXServiceURL jmxServiceUrl = new JMXServiceURL(url);
        JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceUrl);
        MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();

        ObjectInstance threadInstance = mBeanServerConnection.queryMBeans(new ObjectName("*:type=Threading,*"), null).iterator().next();
        ObjectInstance osInstance = mBeanServerConnection.queryMBeans(new ObjectName("*:type=OperatingSystem,*"), null).iterator().next();
        Set<ObjectInstance> gcInstances = mBeanServerConnection.queryMBeans(new ObjectName("*:type=GarbageCollector,*"), null);

        ThreadMXBean threadMXBean = JMX.newMXBeanProxy(mBeanServerConnection, threadInstance.getObjectName(), ThreadMXBean.class, false);
        OperatingSystemMXBean operatingSystemMXBean = JMX.newMXBeanProxy(mBeanServerConnection, osInstance.getObjectName(), OperatingSystemMXBean.class, false);
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = new ArrayList<>();
        for (final ObjectInstance gcInstance : gcInstances) {
            GarbageCollectorMXBean garbageCollectorMXBean = JMX.newMXBeanProxy(mBeanServerConnection, gcInstance.getObjectName(), GarbageCollectorMXBean.class, true);
            garbageCollectorMXBeans.add(garbageCollectorMXBean);
        }

        GCCollector gcCollector = new GCCollector(threadMXBean, operatingSystemMXBean, garbageCollectorMXBeans);
    }

    public void unregisterCollector(final String address) {
    }

    public Object[][][] getGCData(final String address) {
        return null;
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

}