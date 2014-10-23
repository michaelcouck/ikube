package ikube.application;

import com.sun.management.GcInfo;
import ikube.IConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.springframework.util.ReflectionUtils.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
public class GCMXBeanAnalyzer {

    class GCSnapshot {
        double start;
        double end;
        double duration;

        double interval;
        double delta;
        double available;

        double timeToFailure = Long.MAX_VALUE;
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(GCMXBeanAnalyzer.class);
    public static final String EDEN_SPACE = "PS Eden Space";
    public static final String SURVIVOR_SPACE = "PS Survivor Space";
    public static final String PERM_GEN = "PS Perm Gen";
    public static final String OLD_GEN = "PS Old Gen";
    public static final String CODE_CACHE = "Code Cache";

    LinkedList<GCSnapshot> gcSnapshots = new LinkedList<>();

    public void analyze() {
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (final GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            addNotificationListener(garbageCollectorMXBean);
        }
    }

    private void addNotificationListener(final GarbageCollectorMXBean garbageCollectorMXBean) {
        NotificationBroadcaster notificationBroadcaster = (NotificationBroadcaster) garbageCollectorMXBean;
        notificationBroadcaster.addNotificationListener(new NotificationListener() {
            @Override
            public void handleNotification(final Notification notification, final Object handback) {
                LOGGER.debug("User data : " + notification.getUserData());
                getGcSnapshots(garbageCollectorMXBean);
            }
        }, new NotificationFilter() {
            @Override
            public boolean isNotificationEnabled(final Notification notification) {
                LOGGER.debug("Notification type : " + notification.getType());
                return true;
            }
        }, null);
    }

    private void getGcSnapshots(final GarbageCollectorMXBean garbageCollectorMXBean) {
        doWithMethods(garbageCollectorMXBean.getClass(), new MethodCallback() {
            @Override
            public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException {
                try {
                    boolean isAccessible = method.isAccessible();
                    method.setAccessible(Boolean.TRUE);
                    GcInfo gcInfo = (GcInfo) method.invoke(garbageCollectorMXBean);
                    if (gcInfo != null) {
                        GCSnapshot previousGcSnapshot = gcSnapshots.isEmpty() ? null : gcSnapshots.getLast();
                        GCSnapshot gcSnapshot = getGcSnapshot(previousGcSnapshot, gcInfo);
                        gcSnapshots.add(gcSnapshot);
                    }
                    method.setAccessible(isAccessible);
                } catch (final InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new MethodFilter() {
            @Override
            public boolean matches(final Method method) {
                return method.getName().equals("getLastGcInfo");
            }
        });
    }

    private GCSnapshot getGcSnapshot(final GCSnapshot previousGcSnapshot, final GcInfo gcInfo) {

        // start - previousStart = interval <, duration >, before - after = delta <, after - max = available <

        GCSnapshot gcSnapshot = new GCSnapshot();

        Map<String, MemoryUsage> usageMap = gcInfo.getMemoryUsageBeforeGc();
        MemoryUsage memoryUsage = usageMap.get(OLD_GEN);

        gcSnapshot.start = gcInfo.getStartTime();
        gcSnapshot.end = gcInfo.getEndTime();
        gcSnapshot.duration = gcInfo.getDuration();
        gcSnapshot.available = memoryUsage.getMax() - memoryUsage.getUsed();

        if (previousGcSnapshot != null) {
            gcSnapshot.interval = gcSnapshot.start - previousGcSnapshot.start;
            gcSnapshot.delta = previousGcSnapshot.available - gcSnapshot.available;
        }

        LOGGER.error(
                "Interval : " + gcSnapshot.interval +
                        ", duration : " + gcSnapshot.duration +
                        ", delta : " + (gcSnapshot.delta / IConstants.MILLION) +
                        ", available : " + (gcSnapshot.available / IConstants.MILLION));

        return gcSnapshot;
    }

}