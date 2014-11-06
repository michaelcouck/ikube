package ikube.application;

import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.GcInfo;
import ikube.AbstractTest;
import org.junit.Test;
import org.mockito.Mock;

import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
public class GCCollectorTest extends AbstractTest {

    @Mock
    private GcInfo gcInfo;
    @Mock
    private MemoryUsage memoryUsage;
    @Mock
    private ThreadMXBean threadMxBean;
    @Mock
    private OperatingSystemMXBean operatingSystemMxBean;
    @Mock
    private GarbageCollectorMXBean garbageCollectorMxBean;

    @Test
    public void getGcSnapshot() {
        String memoryBlock = "Eden Space";

        Map<String, MemoryUsage> memoryUsageMap = new HashMap<>();
        memoryUsageMap.put(memoryBlock, memoryUsage);

        when(garbageCollectorMxBean.getLastGcInfo()).thenReturn(gcInfo);

        when(gcInfo.getMemoryUsageBeforeGc()).thenReturn(memoryUsageMap);
        when(gcInfo.getMemoryUsageAfterGc()).thenReturn(memoryUsageMap);
        when(gcInfo.getStartTime()).thenReturn(0l);
        when(gcInfo.getEndTime()).thenReturn(1000l);
        // when(gcInfo.getDuration()).thenReturn(gcInfo.getEndTime() - gcInfo.getStartTime()); // Final

        when(memoryUsage.getMax()).thenReturn(1000l);
        // when(memoryUsage.getUsed()).thenReturn((long) (memoryUsage.getMax() * 0.75d)); // Final

        when(operatingSystemMxBean.getSystemLoadAverage()).thenReturn(0.9d);
        when(operatingSystemMxBean.getAvailableProcessors()).thenReturn(8);

        // when(threadMxBean.getThreadCount()).thenReturn(operatingSystemMxBean.getAvailableProcessors() * 10); // Final

        GCCollector gcCollector = new GCCollector(memoryBlock, threadMxBean, operatingSystemMxBean, garbageCollectorMxBean);
        GCSnapshot gcSnapshot = gcCollector.getGcSnapshot();

        assertEquals(memoryUsage.getMax() - memoryUsage.getUsed(), gcSnapshot.available, 0);

        for (int i = (int) GCCollector.PRUNE_THRESHOLD; i > 0; i--) {
            gcCollector.getGcSnapshot();
        }
        logger.error("Size : " + gcCollector.getGcSnapshots().size());

        assertEquals(75001, gcCollector.getGcSnapshots().size());
    }

}
