package ikube.application;

import com.sun.management.GarbageCollectorMXBean;
import ikube.AbstractTest;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.*;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
@Ignore
public class GCAnalyzerTest extends AbstractTest {

    @Spy
    @InjectMocks
    private GCAnalyzer gcAnalyzer;
    @Mock
    private MBeanServerConnection mBeanConnectionServer;
    @Mock
    private ThreadMXBean threadMXBean;
    @Mock
    private OperatingSystemMXBean operatingSystemMXBean;
    @Mock
    private GarbageCollectorMXBean garbageCollectorMXBean;

    private List<GarbageCollectorMXBean> garbageCollectorMXBeans;

    @Before
    public void before() {
        garbageCollectorMXBeans = new ArrayList<>();
    }

    @Test
    public void registerCollector() throws Exception {
        int port = 8500;
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return mBeanConnectionServer;
            }
        }).when(gcAnalyzer).getMBeanServerConnection(address, port);

        when(gcAnalyzer.getMBeanServerConnection(address, port)).thenReturn(mBeanConnectionServer);


        // when(gcAnalyzer.getThreadMXBean(mBeanConnectionServer)).thenReturn(threadMXBean);
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return threadMXBean;
            }
        }).when(gcAnalyzer).getThreadMXBean(mBeanConnectionServer);

        when(gcAnalyzer.getOperatingSystemMXBean(mBeanConnectionServer)).thenReturn(operatingSystemMXBean);
        when(gcAnalyzer.getGarbageCollectorMXBeans(mBeanConnectionServer)).thenReturn(garbageCollectorMXBeans);
        garbageCollectorMXBeans.add(garbageCollectorMXBean);

        // ObjectInstance objectInstance = null;
        // when(mBeanConnectionServer.queryMBeans(any(ObjectName.class), null)).thenReturn(new TreeSet<ObjectInstance>(Arrays.asList(objectInstance)));

        gcAnalyzer.registerCollector(address, port);
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