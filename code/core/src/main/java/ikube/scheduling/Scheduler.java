package ikube.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class just schedules events to be fired a a particular rate. Listeners can then respond to the events.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class Scheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

	/** The list of schedules. */
	private List<Schedule> schedules;
	/** The scheduler. */
	private ScheduledExecutorService scheduledExecuterService;

	/**
	 * Iterates over the schedules scheduling them for execution.
	 */
	public void initialize() {
		LOGGER.info("Scheduler : ");
		if (scheduledExecuterService != null) {
			shutdown();
		}
		scheduledExecuterService = Executors.newScheduledThreadPool(100);
		for (final Schedule schedule : schedules) {
			try {
				schedule(schedule);
			} catch (Exception e) {
				LOGGER.error("Exception scheduling the events : ", e);
			}
		}
	}

	public ScheduledFuture<?> schedule(final Schedule schedule) {
		LOGGER.info("Scheduling : " + schedule);
		if (schedule.isSingle()) {
			return scheduledExecuterService.schedule(schedule, schedule.getDelay(), TimeUnit.MILLISECONDS);
		} else {
			return scheduledExecuterService.scheduleAtFixedRate(schedule, schedule.getDelay(), schedule.getPeriod(), TimeUnit.MILLISECONDS);
		}
	}

	public void shutdown() {
		this.schedules.clear();
		this.scheduledExecuterService.shutdown();
		List<Runnable> runnables = this.scheduledExecuterService.shutdownNow();
		LOGGER.info("Shutdown schedules : " + runnables);
	}

	public void setSchedule(final Schedule schedule) {
		if (this.schedules == null) {
			this.schedules = new ArrayList<Schedule>();
		}
		this.schedules.add(schedule);
	}

	public void setSchedules(final List<Schedule> schedules) {
		this.schedules = schedules;
	}

}