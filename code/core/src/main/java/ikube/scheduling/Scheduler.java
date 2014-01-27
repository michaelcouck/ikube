package ikube.scheduling;

import ikube.IConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class just schedules threads to execute the run methods.
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
	private ScheduledExecutorService service;

	/**
	 * Iterates over the schedules scheduling them for execution.
	 */
	public void initialize() {
		if (service != null) {
			shutdown();
		}
		service = Executors.newScheduledThreadPool(IConstants.THREAD_POOL_SIZE);
		for (final Schedule schedule : schedules) {
			try {
				schedule(schedule);
			} catch (Exception e) {
				LOGGER.error("Exception scheduling the events : ", e);
			}
		}
	}

	public ScheduledFuture<?> schedule(final Schedule schedule) {
		if (schedule.isSingle()) {
			return service.schedule(schedule, schedule.getDelay(), TimeUnit.MILLISECONDS);
		} else {
			return service.scheduleAtFixedRate(schedule, schedule.getDelay(), schedule.getPeriod(), TimeUnit.MILLISECONDS);
		}
	}

	public void shutdown() {
		this.schedules.clear();
		this.service.shutdown();
		List<Runnable> runnables = this.service.shutdownNow();
		LOGGER.debug("Shutdown schedules : " + runnables);
	}

	public void setSchedule(final Schedule schedule) {
		if (this.schedules == null) {
			this.schedules = new ArrayList<>();
		}
		this.schedules.add(schedule);
	}

	public void setSchedules(final List<Schedule> schedules) {
		this.schedules = schedules;
	}

}