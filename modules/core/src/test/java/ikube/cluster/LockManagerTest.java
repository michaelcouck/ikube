package ikube.cluster;

import static org.junit.Assert.assertEquals;
import ikube.ATest;
import ikube.model.Event;

import java.util.ArrayList;
import java.util.List;

import org.jgroups.JChannel;
import org.junit.Test;

public class LockManagerTest extends ATest {

	private JChannel channel;

	@Test
	public void start() throws Exception {
		final LockManager lockManagerOne = new LockManager();
		final LockManager lockManagerTwo = new LockManager();
		final LockManager lockManagerThree = new LockManager();
		final Event event = new Event();
		event.setType(Event.CLUSTERING);
		final long sleep = 100;
		final List<Exception> exceptions = new ArrayList<Exception>();
		Thread thread = new Thread(new Runnable() {
			public void run() {
				int iterations = 50;
				while (--iterations >= 0) {
					try {
						if (iterations == 35) {
							lockManagerOne.close();
						}
						if (iterations == 20) {
							// Open the closed lock manager again
							lockManagerOne.open();
						}
						if (iterations == 5) {
							// Close number two
							lockManagerTwo.close();
						}
						lockManagerOne.handleNotification(event);
						Thread.sleep(sleep);
						lockManagerTwo.handleNotification(event);
						Thread.sleep(sleep);
						lockManagerThree.handleNotification(event);
						Thread.sleep(sleep);

					} catch (Exception e) {
						exceptions.add(e);
					}
				}
			}
		});
		thread.start();
		thread.join();
		assertEquals(0, exceptions.size());
	}

	protected void printDetails() {
		logger.info("Queue : " + channel.dumpQueue());
		logger.info("Timer queue : " + channel.dumpTimerQueue());
		logger.info("Address : " + channel.getAddressAsString());
		logger.info("Uuid : " + channel.getAddressAsUUID());
		logger.info("Tasks : " + channel.getNumberOfTasksInTimer());
		logger.info("Messages : " + channel.getNumMessages());
		logger.info("View : " + channel.getView());
		logger.info("Num messages : " + channel.getNumMessages());
		// logger.info("Up : " + channel.up(new Event(Event.BLOCK)));
		// logger.info("Up : " + channel.up(new Event(Event.BLOCK)));
		// logger.info("Up : " + channel.up(new Event(Event.UNBLOCK)));
		// logger.info("Up : " + channel.up(new Event(Event.UNBLOCK)));
	}

}
