package ikube.scheduling.listener;

import ikube.IConstants;
import ikube.toolkit.ThreadUtilities;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author Michael Couck
 * @since 28.03.13
 * @version 01.00
 */
public class MemoryListener implements IListener {

	@Value("${max.memory}")
	private String maxMemory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleNotification(final Event event) {
		if (!Event.MEMORY_SIZE.equals(event.getType())) {
			return;
		}
		if (Runtime.getRuntime().totalMemory() / IConstants.MILLION > Integer.parseInt(maxMemory)) {
			new ThreadUtilities().destroy();
		}
	}

}