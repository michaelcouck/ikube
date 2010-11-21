package ikube.toolkit;

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

	public Scheduler(List<Timer> runnables) {
		this.logger = Logger.getLogger(this.getClass());
		for (Timer runnable : runnables) {
			try {
				scheduler.scheduleAtFixedRate(runnable, runnable.getDelay(), runnable.getPeriod(), TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

}
