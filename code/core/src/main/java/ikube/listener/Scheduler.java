package ikube.listener;

import java.util.ArrayList;
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

	private static Logger LOGGER;
	/** The scheduler. */
	private static transient ScheduledExecutorService SCHEDULER;
	/** The list of schedules. */
	private static transient List<Schedule> SCHEDULES = new ArrayList<Schedule>();

	/**
	 * Iterates over the schedules scheduling them for execution.
	 */
	public static void initialize() {
		LOGGER = Logger.getLogger(Scheduler.class);
		SCHEDULER = Executors.newScheduledThreadPool(10);
		for (final Schedule schedule : SCHEDULES) {
			try {
				SCHEDULER.scheduleAtFixedRate(new Runnable() {
					public void run() {
						Event event = new Event();
						event.setType(schedule.getType());
						event.setConsumed(Boolean.FALSE);
						event.setTimestamp(System.currentTimeMillis());
						ListenerManager.fireEvent(event);
					}
				}, schedule.getDelay(), schedule.getPeriod(), TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				LOGGER.error("Exception scheduling the events : ", e);
			}
		}
	}

	public static void shutdown() {
		Scheduler.SCHEDULER.shutdown();
		Scheduler.SCHEDULES.clear();
	}

	public static void addSchedule(Schedule schedule) {
		Scheduler.SCHEDULES.add(schedule);
	}

}
