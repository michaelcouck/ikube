package ikube.experimental.publish;

import ikube.experimental.IConstants;
import ikube.experimental.listener.SystemMonitoringEvent;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Random;

/**
 * This class publishes the processing data to the dashboard.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
@Component
public class SystemMonitoringPublisher extends AbstractPublish<SystemMonitoringEvent> {

    @Override
    public void notify(final SystemMonitoringEvent event) {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        String streamKey = "W6rv4Ee2";
        double systemLoad = (operatingSystemMXBean.getSystemLoadAverage() * 100d) / operatingSystemMXBean.getAvailableProcessors();
        push(IConstants.API_KEY, streamKey, systemLoad);
        logger.debug("Event : {}", event.hashCode());

        streamKey = "9kAZdmOd";
        double latitude = 90d * new Random().nextDouble();
        double longitude = 180d * new Random().nextDouble();
        if (System.currentTimeMillis() % 2 == 0) {
            longitude = -longitude;
        }
        push(IConstants.API_KEY, streamKey, latitude, longitude);
        logger.debug("Event : {}", event.hashCode());
    }

}
