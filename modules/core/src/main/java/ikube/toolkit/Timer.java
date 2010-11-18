package ikube.toolkit;

import ikube.listener.ListenerManager;
import ikube.model.Event;

import java.sql.Timestamp;

import org.apache.log4j.Logger;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class Timer implements Runnable {

	private Logger logger = Logger.getLogger(this.getClass());
	private String type;
	private long delay;
	private long period;
	private PooledExecutor pooledExecutor;
	private int threads = 1;

	public Timer() {
		this.pooledExecutor = new PooledExecutor();
		this.pooledExecutor.createThreads(threads);
	}

	@Override
	public void run() {
		try {
			this.pooledExecutor.execute(new Runnable() {
				public void run() {
					Event event = new Event();
					event.setType(type);
					event.setConsumed(Boolean.FALSE);
					event.setTimestamp(new Timestamp(System.currentTimeMillis()));
					ListenerManager.fireEvent(event);
				}
			});
		} catch (Exception e) {
			logger.error("Exception running the timer : ", e);
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

}
