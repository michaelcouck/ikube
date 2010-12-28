package ikube.listener;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class SchedulerTest extends ATest {

	private boolean notified = Boolean.FALSE;

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

		Scheduler scheduler = new Scheduler();
		Schedule schedule = mock(Schedule.class);
		when(schedule.getDelay()).thenReturn(100l);
		when(schedule.getPeriod()).thenReturn(1000000l);
		when(schedule.getType()).thenReturn(Event.TIMER);
		scheduler.setSchedules(Arrays.asList(schedule));
		scheduler.initialize();
		Thread.sleep(1000);

		assertTrue(notified);
	}

	private void notified() {
		this.notified = Boolean.TRUE;
	}

}
