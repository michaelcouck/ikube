package ikube.listener;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.ATest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class SchedulerTest extends ATest {

	private Scheduler	scheduler;

	public SchedulerTest() {
		super(SchedulerTest.class);
	}

	@Before
	public void before() {
		scheduler = new Scheduler();
	}

	@After
	public void after() {
		scheduler.shutdown();
	}

	@Test
	public void initialize() throws Exception {
		ListenerManager listenerManager = mock(ListenerManager.class);
		Schedule schedule = mock(Schedule.class);
		when(schedule.getDelay()).thenReturn(10l);
		when(schedule.getPeriod()).thenReturn(10l);
		when(schedule.getType()).thenReturn(Event.TIMER);

		scheduler.setListenerManager(listenerManager);
		scheduler.setSchedule(schedule);
		scheduler.initialize();

		Thread.sleep(1000);
		verify(listenerManager, Mockito.atLeastOnce()).fireEvent(any(Event.class));

		Thread.sleep(1000);
		verify(listenerManager, Mockito.atLeast(10)).fireEvent(any(Event.class));
	}

}
