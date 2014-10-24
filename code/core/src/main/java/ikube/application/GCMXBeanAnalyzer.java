package ikube.application;

import com.sun.management.GcInfo;
import ikube.IConstants;
import ikube.toolkit.ThreadUtilities;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.springframework.util.ReflectionUtils.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
class GCMXBeanAnalyzer {

    public static final Logger LOGGER = LoggerFactory.getLogger(GCMXBeanAnalyzer.class);

    public static final String EDEN_SPACE = "PS Eden Space";
    public static final String PERM_GEN = "PS Perm Gen";
    public static final String OLD_GEN = "PS Old Gen";
    public static final String[] MEMORY_BLOCKS = {EDEN_SPACE, /* SURVIVOR_SPACE, */ PERM_GEN, OLD_GEN /* , CODE_CACHE */};

    @SuppressWarnings("UnusedDeclaration")
    public static final String SURVIVOR_SPACE = "PS Survivor Space";
    @SuppressWarnings("UnusedDeclaration")
    public static final String CODE_CACHE = "Code Cache";

    Map<String, LinkedList<GCSnapshot>> gcSnapshots;

    GCMXBeanAnalyzer() {
        gcSnapshots = new HashMap<>();
        for (final String memoryBlock : MEMORY_BLOCKS) {
            gcSnapshots.put(memoryBlock, new LinkedList<GCSnapshot>());
        }
        ThreadUtilities.submit("gc-analyzer", new GCAnalyzer(gcSnapshots));
    }

    void analyze() {
        registerMemoryPoolListeners();
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (final GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            registerGcNotificationListener(garbageCollectorMXBean);
        }
    }

    /**
     * This method is useless because the {@link java.lang.management.MemoryMXBean} never emits events, and
     * the thresholds for the memory pool beans cannot be touched as any interference, even calling methods that
     * return longs will result in the thread being deadlocked.
     */
    private void registerMemoryPoolListeners() {
        MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
        NotificationEmitter notificationEmitter = (NotificationEmitter) memoryMxBean;
        notificationEmitter.addNotificationListener(new NotificationListener() {
            public void handleNotification(final Notification notification, final Object target) {
                LOGGER.warn("Memory threshold exceeded : " + notification.getMessage() + ", target : " + target);
            }
        }, new NotificationFilter() {
            @Override
            public boolean isNotificationEnabled(final Notification notification) {
                return true;
                // return notification.getType().equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED);
            }
        }, "Michael Couck");
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        //noinspection StatementWithEmptyBody
        for (final MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            // Below here deadlocks the thread
            // memoryPoolMXBean.setUsageThreshold((long) (memoryPoolMXBean.getUsageThreshold() * 0.8));
            // memoryPoolMXBean.setCollectionUsageThreshold((long) (memoryPoolMXBean.getCollectionUsageThreshold() * 0.8));
        }
    }

    private void registerGcNotificationListener(final GarbageCollectorMXBean garbageCollectorMXBean) {
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
                        for (final String memoryBlock : MEMORY_BLOCKS) {
                            LinkedList<GCSnapshot> snapshots = gcSnapshots.get(memoryBlock);
                            GCSnapshot previousGcSnapshot = snapshots.isEmpty() ? null : snapshots.getLast();
                            GCSnapshot gcSnapshot = getGcSnapshot(memoryBlock, previousGcSnapshot, gcInfo);
                            if (gcSnapshot != null) {
                                snapshots.add(gcSnapshot);
                            }
                        }
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

    private GCSnapshot getGcSnapshot(final String memoryBlock, final GCSnapshot previousGcSnapshot, final GcInfo gcInfo) {
        // If the gc is running 10 times a second it is not helpful,
        // so we'll increment the previous runsPerSecond and return null
        if (previousGcSnapshot != null) {
            if (gcInfo.getStartTime() - previousGcSnapshot.start < 1000) {
                previousGcSnapshot.runsPerSecond++;
                return null;
            }
        }

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        Map<String, MemoryUsage> usageMap = gcInfo.getMemoryUsageBeforeGc();
        MemoryUsage memoryUsage = usageMap.get(memoryBlock);

        GCSnapshot gcSnapshot = new GCSnapshot();
        gcSnapshot.start = gcInfo.getStartTime();
        gcSnapshot.end = gcInfo.getEndTime();
        gcSnapshot.duration = gcInfo.getDuration();
        gcSnapshot.available = memoryUsage.getMax() - memoryUsage.getUsed();
        gcSnapshot.usedToMaxRatio = (double) memoryUsage.getUsed() / (double) memoryUsage.getMax();
        gcSnapshot.cpuLoad = operatingSystemMXBean.getSystemLoadAverage();
        gcSnapshot.processors = operatingSystemMXBean.getAvailableProcessors();
        gcSnapshot.perCoreLoad = gcSnapshot.cpuLoad / gcSnapshot.processors;
        gcSnapshot.threads = threadMXBean.getThreadCount();

        if (previousGcSnapshot != null) {
            gcSnapshot.interval = gcSnapshot.start - previousGcSnapshot.start;
            gcSnapshot.delta = previousGcSnapshot.available - gcSnapshot.available;
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        LOGGER.error(
                "Type : " + StringUtils.substring(memoryBlock, 0, 10) +
                        ", int : " + decimalFormat.format(gcSnapshot.interval) +
                        ", dura : " + decimalFormat.format(gcSnapshot.duration) +
                        ", delta : " + decimalFormat.format((gcSnapshot.delta / IConstants.MILLION)) +
                        ", avail : " + decimalFormat.format((gcSnapshot.available / IConstants.MILLION)) +
                        ", use/max : " + decimalFormat.format(gcSnapshot.usedToMaxRatio) +
                        /* ", run/sec : " + gcSnapshot.runsPerSecond + */
                        ", cpu : " + decimalFormat.format(gcSnapshot.cpuLoad) +
                        ", cpu/core: " + decimalFormat.format(gcSnapshot.perCoreLoad) +
                        ", threads : " + gcSnapshot.threads);

        return gcSnapshot;
    }

}