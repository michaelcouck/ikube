package ikube.scheduling.listener;

import ikube.scheduling.Schedule;
import ikube.toolkit.ThreadUtilities;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Similar to the termination listener, this listener waits for an event triggering the start up of the executer service, to allow actions
 * to be submitted for execution.
 * 
 * @author Michael couck
 * @since 24.12.11
 * @version 01.00
 */
public class StartupListener extends Schedule {

	@Autowired
	private ThreadUtilities threadUtilities;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		threadUtilities.initialize();
	}

}
