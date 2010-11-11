package ikube.toolkit;

import ikube.listener.ListenerManager;
import ikube.model.Event;

import java.sql.Timestamp;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class Timer extends TimerTask {

	private Logger logger = Logger.getLogger(this.getClass());
	private String type;
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
					logger.debug("Firing start event : " + event);
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

}
