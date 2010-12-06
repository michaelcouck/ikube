package ikube.index.handler;

import ikube.model.Indexable;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public abstract class Handler implements IHandler<Indexable<?>> {

	protected Logger logger = Logger.getLogger(this.getClass());

	private int threads;

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

}
