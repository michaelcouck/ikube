package ikube.listener;

import ikube.model.Event;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class Scheduler {

	private Logger logger;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

	public Scheduler(List<Schedule> schedules) {
		this.logger = Logger.getLogger(this.getClass());
		for (final Schedule schedule : schedules) {
			try {
				scheduler.scheduleAtFixedRate(new Runnable() {
					public void run() {
						Event event = new Event();
						event.setType(schedule.getType());
						event.setConsumed(Boolean.FALSE);
						event.setTimestamp(new Timestamp(System.currentTimeMillis()));
						ListenerManager.fireEvent(event);
					}
				}, schedule.getDelay(), schedule.getPeriod(), TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

}
