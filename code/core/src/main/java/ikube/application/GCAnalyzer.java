package ikube.application;

import ikube.analytics.IAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
@SuppressWarnings({"SpringJavaAutowiredMembersInspection", "unchecked"})
public class GCAnalyzer {

    static final Logger LOGGER = LoggerFactory.getLogger(GCAnalyzer.class);

    private Map<String, GCCollector> gcCollectorMap = new HashMap<>();

    @Autowired
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    private IAnalyticsService analyticsService;

    public void registerCollector(final String address) throws IOException {
        // String url = "service:jmx:rmi:///jndi/rmi://192.168.1.20:8500/jmxrmi";
        // String url = "service:jmx:rmi:///jndi/rmi://ikube.be:8500/jmxrmi";
        // String url = "service:jmx:rmi://192.168.1.20:1099/jndi/rmi://192.168.1.20:8500/jmxrmi";
        // String url = "service:jmx:rmi://81.82.213.177:1099/jndi/rmi://81.82.213.177:8500/jmxrmi";
        // 81.82.213.177:8500

        String url = "service:jmx:rmi://has-no-effect:1099/jndi/rmi://192.168.1.20:8500/jmxrmi";
        JMXServiceURL jmxServiceUrl = new JMXServiceURL(url);
        JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceUrl);
    }

    public void unregisterCollector(final String address) {
    }

    public Object[][][] getGCData(final String address) {
        return null;
    }

}