package ikube.scheduling.listener;

import ikube.IConstants;
import ikube.scheduling.Schedule;
import ikube.toolkit.ThreadUtilities;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author Michael Couck
 * @since 28.03.13
 * @version 01.00
 */
public class MemoryListener extends Schedule {

	@Value("${max.memory}")
	private String maxMemory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		if (Runtime.getRuntime().totalMemory() / IConstants.MILLION > Integer.parseInt(maxMemory)) {
			new ThreadUtilities().destroy();
		}
	}

}