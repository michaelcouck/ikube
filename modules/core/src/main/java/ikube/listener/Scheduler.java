package ikube.listener;


import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * This class just schedules events to be fired a a particular rate. Listeners can then respond to the events.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class Scheduler {

	private Logger logger;
	/** The scheduler. */
	private ScheduledExecutorService scheduler;
	/** The list of schedules. */
	private List<Schedule> schedules;

	/**
	 * Iterates over the schedules scheduling them for execution.
	 */
	protected void initialize() {
		this.logger = Logger.getLogger(this.getClass());
		scheduler = Executors.newScheduledThreadPool(10);
		for (final Schedule schedule : schedules) {
			try {
				scheduler.scheduleAtFixedRate(new Runnable() {
					public void run() {
						Event event = new Event();
						event.setType(schedule.getType());
						event.setConsumed(Boolean.FALSE);
						event.setTimestamp(System.currentTimeMillis());
						ListenerManager.fireEvent(event);
					}
				}, schedule.getDelay(), schedule.getPeriod(), TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				logger.error("Exception scheduling the events : ", e);
			}
		}
	}

	public void setSchedules(List<Schedule> schedules) {
		this.schedules = schedules;
	}

}
