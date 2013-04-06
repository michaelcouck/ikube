package ikube.scheduling;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class SchedulerTest extends AbstractTest {

	public SchedulerTest() {
		super(SchedulerTest.class);
	}

	@Test
	public void initialize() throws Exception {
		Scheduler scheduler = new Scheduler();
		try {
			Schedule schedule = mock(Schedule.class);
			when(schedule.getDelay()).thenReturn(10l);
			when(schedule.getPeriod()).thenReturn(10l);

			scheduler.setSchedule(schedule);
			scheduler.initialize();

			Thread.sleep(1000);
			verify(schedule, Mockito.atLeastOnce()).run();
		} finally {
			scheduler.shutdown();
		}
	}

}