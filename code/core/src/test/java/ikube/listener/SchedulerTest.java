package ikube.listener;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class SchedulerTest extends ATest {

	private boolean notified = Boolean.FALSE;

	public SchedulerTest() {
		super(SchedulerTest.class);
	}

	@Test
	public void initialize() throws Exception {
		IListener listener = mock(IListener.class);
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				notified();
				return null;
			}
		}).when(listener).handleNotification(any(Event.class));

		ListenerManager.addListener(listener);

		Schedule schedule = mock(Schedule.class);
		when(schedule.getDelay()).thenReturn(100l);
		when(schedule.getPeriod()).thenReturn(1000000l);
		when(schedule.getType()).thenReturn(Event.TIMER);
		Scheduler.addSchedule(schedule);
		Scheduler.initialize();
		Thread.sleep(1000);

		assertTrue(notified);
	}

	private void notified() {
		this.notified = Boolean.TRUE;
	}

}
