package ikube.application;

import com.sun.management.GcInfo;
import ikube.IConstants;
import ikube.toolkit.ThreadUtilities;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.util.ReflectionUtils.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
class GCCollector implements Serializable {

    public static final Logger LOGGER = LoggerFactory.getLogger(GCCollector.class);

    static final String EDEN_SPACE = "PS Eden Space"; // New object, and stack
    static final String PERM_GEN = "PS Perm Gen"; // Class loading area
    static final String OLD_GEN = "PS Old Gen"; // Tenured space, permanent

    /**
     * The memory areas that we will monitor.
     */
    static final String[] MEMORY_BLOCKS = {EDEN_SPACE, /* SURVIVOR_SPACE, */ PERM_GEN, OLD_GEN /* , CODE_CACHE */};

    @SuppressWarnings("UnusedDeclaration")
    static final String SURVIVOR_SPACE = "PS Survivor Space";
    @SuppressWarnings("UnusedDeclaration")
    static final String CODE_CACHE = "Code Cache";

    private transient ThreadMXBean threadMXBean;
    private transient OperatingSystemMXBean operatingSystemMXBean;

    GCCollector(final ThreadMXBean threadMXBean, final OperatingSystemMXBean operatingSystemMXBean,
                final List<GarbageCollectorMXBean> garbageCollectorMXBeans) {

        this.threadMXBean = threadMXBean;
        this.operatingSystemMXBean = operatingSystemMXBean;

        Map<String, LinkedList<GCSnapshot>> gcSnapshots = new HashMap<>();
        for (final String memoryBlock : MEMORY_BLOCKS) {
            gcSnapshots.put(memoryBlock, new LinkedList<GCSnapshot>());
        }
        for (final GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            registerGcNotificationListener(garbageCollectorMXBean, gcSnapshots);
        }
    }

    private void registerGcNotificationListener(final GarbageCollectorMXBean garbageCollectorMXBean, final Map<String, LinkedList<GCSnapshot>> gcSnapshots) {
        ThreadUtilities.submit(garbageCollectorMXBean.toString(), new Runnable() {
            @Override
            @SuppressWarnings("InfiniteLoopStatement")
            public void run() {
                while (true) {
                    getGcSnapshots(garbageCollectorMXBean, gcSnapshots);
                    ThreadUtilities.sleep(250);
                }
            }
        });
    }

    private void getGcSnapshots(final GarbageCollectorMXBean garbageCollectorMXBean, final Map<String, LinkedList<GCSnapshot>> gcSnapshots) {
        final AtomicReference<GcInfo> gcInfoAtomicReference = new AtomicReference<>();
        doWithMethods(garbageCollectorMXBean.getClass(), new MethodCallback() {
            @Override
            public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException {
                try {
                    boolean isAccessible = method.isAccessible();
                    method.setAccessible(Boolean.TRUE);
                    GcInfo gcInfo = (GcInfo) method.invoke(garbageCollectorMXBean);
                    gcInfoAtomicReference.set(gcInfo);
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

        GcInfo gcInfo = gcInfoAtomicReference.get();
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
    }

    private GCSnapshot getGcSnapshot(final String memoryBlock, final GCSnapshot previousGcSnapshot, final GcInfo gcInfo) {
        // If the gc is running 10 times a second it is not helpful,
        // so we'll increment the previous runsPerTimeUnit and return null
        if (previousGcSnapshot != null) {
            if (gcInfo.getStartTime() - previousGcSnapshot.start < 1000) {
                previousGcSnapshot.runsPerTimeUnit++;
                return null;
            }
        }

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
        LOGGER.warn(
                "Type : " + StringUtils.substring(memoryBlock, 0, 10) +
                        ", int : " + decimalFormat.format(gcSnapshot.interval) +
                        ", dura : " + decimalFormat.format(gcSnapshot.duration) +
                        ", delta : " + decimalFormat.format((gcSnapshot.delta / IConstants.MILLION)) +
                        ", avail : " + decimalFormat.format((gcSnapshot.available / IConstants.MILLION)) +
                        ", use/max : " + decimalFormat.format(gcSnapshot.usedToMaxRatio) +
                        /* ", run/sec : " + gcSnapshot.runsPerTimeUnit + */
                        ", cpu : " + decimalFormat.format(gcSnapshot.cpuLoad) +
                        ", cpu/core: " + decimalFormat.format(gcSnapshot.perCoreLoad) +
                        ", threads : " + gcSnapshot.threads);

        return gcSnapshot;
    }

}