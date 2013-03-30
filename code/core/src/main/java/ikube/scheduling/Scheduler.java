package ikube.scheduling;

import ikube.scheduling.listener.Event;
import ikube.scheduling.listener.ListenerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
	/** The listener manager that will notify the listeners when a schedule is triggered. */
	private ListenerManager listenerManager;
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
				LOGGER.info("Scheduling : " + schedule);
				scheduledExecuterService.scheduleAtFixedRate(new Runnable() {
					public void run() {
						Event event = new Event();
						event.setType(schedule.getType());
						event.setConsumed(Boolean.FALSE);
						event.setTimestamp(System.currentTimeMillis());
						listenerManager.fireEvent(event);
					}
				}, schedule.getDelay(), schedule.getPeriod(), TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				LOGGER.error("Exception scheduling the events : ", e);
			}
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

	public void setListenerManager(final ListenerManager listenerManager) {
		this.listenerManager = listenerManager;
	}

}