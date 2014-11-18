package ikube.application;

import com.sun.management.GarbageCollectorMXBean;
import ikube.AbstractTest;
import ikube.scanner.Scanner;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
public class GCAnalyzerTest extends AbstractTest {

    @Spy
    @InjectMocks
    private GCAnalyzer gcAnalyzer;
    @Mock
    private JMXConnector jmxConnector;
    @Mock
    private MBeanServerConnection mBeanConnectionServer;
    @Mock
    private ThreadMXBean threadMXBean;
    @Mock
    private OperatingSystemMXBean operatingSystemMXBean;
    @Mock
    private GarbageCollectorMXBean garbageCollectorMXBean;
    @Mock
    private GCCollector edenCollector;
    @Mock
    private GCCollector permCollector;
    @Mock
    private GCCollector oldCollector;
    @Mock
    private GCSmoother gcSmoother;
    @Mock
    private Scanner scanner;

    private int port = 8500;
    private List<GarbageCollectorMXBean> garbageCollectorMXBeans;

    @Before
    public void before() {
        garbageCollectorMXBeans = new ArrayList<>();
        garbageCollectorMXBeans.add(garbageCollectorMXBean);
    }

    @Test
    public void registerCollectors() throws Exception {
        ArrayList<String> addresses = new ArrayList<>(Arrays.asList("192.168.1.0:8500"));
        when(scanner.scan(anyString(), anyInt())).thenReturn(addresses);
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(gcAnalyzer).registerCollector(any(String.class), anyInt());
        gcAnalyzer.registerCollectors("192.168.1.0");
        verify(gcAnalyzer, times(1)).registerCollector(anyString(), anyInt());
    }

    @Test
    public void registerCollector() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return jmxConnector;
            }
        }).when(gcAnalyzer).getJMXConnector(any(String.class));
        // when(gcAnalyzer.getJMXConnector(address)).thenReturn(jmxConnector);
        when(jmxConnector.getMBeanServerConnection()).thenReturn(mBeanConnectionServer);
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return threadMXBean;
            }
        }).when(gcAnalyzer).getThreadMXBean(mBeanConnectionServer);
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return operatingSystemMXBean;
            }
        }).when(gcAnalyzer).getOperatingSystemMXBean(mBeanConnectionServer);
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return garbageCollectorMXBeans;
            }
        }).when(gcAnalyzer).getGarbageCollectorMXBeans(mBeanConnectionServer);

        gcAnalyzer.registerCollector(address, port);
        assertTrue(gcAnalyzer.gcCollectorMap.size() == 1);
        assertEquals(GCAnalyzer.MEMORY_BLOCKS.length, gcAnalyzer.gcCollectorMap.values().iterator().next().size());
    }

    @Test
    @SuppressWarnings({"NullArgumentToVariableArgMethod", "unchecked", "ResultOfMethodCallIgnored"})
    public void getGcData() {
        List<GCSnapshot> smoothGcSnapshots = Arrays.asList(getGcSnapshot(), getGcSnapshot(), getGcSnapshot());
        when(gcSmoother.getSmoothedSnapshots(any(List.class))).thenReturn(smoothGcSnapshots);
        gcAnalyzer.gcCollectorMap.put(gcAnalyzer.getAddressAndPort(address, port), Arrays.asList(edenCollector, permCollector, oldCollector));

        Object[][][] gcData = gcAnalyzer.getGcData(address, port);
        for (final Object[][] matrix : gcData) {
            for (int i = 0; i < matrix.length; i++) {
                Object[] vector = matrix[i];
                GCSnapshot gcSnapshot = smoothGcSnapshots.get(i);

                assertTrue(vector[0].equals(new Date(gcSnapshot.start)));
                assertTrue(vector[1].equals(gcSnapshot.delta));
                assertTrue(vector[2].equals(gcSnapshot.duration));
                assertTrue(vector[3].equals(gcSnapshot.interval));
                assertTrue(vector[4].equals(gcSnapshot.perCoreLoad));
                assertTrue(vector[5].equals(gcSnapshot.runsPerTimeUnit));
                assertTrue(vector[6].equals(gcSnapshot.usedToMaxRatio));
            }
        }
    }

    GCSnapshot getGcSnapshot() {
        GCSnapshot gcSnapshot = new GCSnapshot();
        gcSnapshot.available = 6;
        gcSnapshot.cpuLoad = 4.32;
        gcSnapshot.delta = -0.23;
        gcSnapshot.duration = 213l;
        gcSnapshot.end = 0;
        gcSnapshot.perCoreLoad = 0.581;
        gcSnapshot.runsPerTimeUnit = 3;
        gcSnapshot.start = 0;
        gcSnapshot.threads = 28;
        gcSnapshot.usedToMaxRatio = 0.568;
        return gcSnapshot;
    }

    @Test
    public void checkTimeUnitConversion() {
        // Check whether the time unit conversion rounds the time unit
        // on overflow, or truncates the excess seconds for the minute to
        // the lower bound
        for (int i = 0; i < 90; i++) {
            long nextSecond = currentTimeMillis() + (i * 1000);
            Date current = new Date(nextSecond);
            long millisToSeconds = MILLISECONDS.toSeconds(nextSecond);
            long millisToMinutes = MILLISECONDS.toMinutes(nextSecond);
            long minutesToMillis = MINUTES.toMillis(millisToMinutes);

            Date minutesToMillisDate = new Date(minutesToMillis);

            logger.debug(current.toString());
            logger.debug(Long.toString(millisToSeconds));
            logger.debug(Long.toString(millisToMinutes));
            logger.debug(minutesToMillisDate.toString());
            logger.debug("");

            long excessSeconds = millisToSeconds % 60l;
            long truncatedSeconds = millisToSeconds - excessSeconds;
            long truncatedMillis = truncatedSeconds * 1000;
            // And the time is truncated in the TimeUnit enumeration logic
            Assert.assertTrue(new Date(truncatedMillis).equals(minutesToMillisDate));
        }
    }

}