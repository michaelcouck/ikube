package ikube.scheduling;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class Schedule implements Runnable {

	private long delay = 10;
	private long period = 10;
	private boolean single = Boolean.FALSE;

	public long getDelay() {
		return delay;
	}

	public void setDelay(final long delay) {
		this.delay = delay;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(final long period) {
		this.period = period;
	}

	public boolean isSingle() {
		return single;
	}

	public void setSingle(boolean single) {
		this.single = single;
	}

}
