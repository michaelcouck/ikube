package ikube.scheduling.schedule;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.toolkit.ThreadUtilities;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 13-08-2014
 */
public class MemoryScheduleTest extends AbstractTest {

    @Spy
    @InjectMocks
    private MemorySchedule memorySchedule;

    @Before
    public void before() {
        ThreadUtilities.initialize();
        Mockit.setUpMock(RuntimeMock.class);
    }

    @After
    public void after() {
        ThreadUtilities.initialize();
        Mockit.tearDownMocks(RuntimeMock.class);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void run() {
        assertTrue(ThreadUtilities.isInitialized());
        memorySchedule.run();
        assertFalse(ThreadUtilities.isInitialized());
    }

    @MockClass(realClass = Runtime.class)
    public static class RuntimeMock {
        @Mock
        public static Runtime getRuntime() {
            Runtime runtime = Mockito.mock(Runtime.class);
            Mockito.when(runtime.totalMemory()).thenReturn(5000l * IConstants.MILLION);
            return runtime;
        }
    }

}