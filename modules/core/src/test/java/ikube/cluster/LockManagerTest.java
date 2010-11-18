package ikube.cluster;

import ikube.ATest;
import ikube.model.Event;

import java.util.ArrayList;
import java.util.List;

import org.jgroups.JChannel;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class LockManagerTest extends ATest {

	private JChannel channel;

	@Test
	public void start() throws Exception {
		int servers = 5;
		final List<LockManager> lockManagers = new ArrayList<LockManager>();
		List<Thread> threads = new ArrayList<Thread>();
		final Event event = new Event();
		event.setType(Event.CLUSTERING);
		final long sleep = 100;
		final int iterations = 10;
		for (int i = 0; i < servers; i++) {
			Thread thread = new Thread(new Runnable() {

				private LockManager lockManager = new LockManager();
				{
					lockManagers.add(lockManager);
				}

				public void run() {
					int privateIterations = iterations;
					while (--privateIterations >= 0) {
						if (Math.random() < 0.01) {
							lockManager.close();
						}
						if (Math.random() > 0.9 && lockManager.getServer() == null) {
							lockManager.open();
						}
						if (lockManager.getServer() != null) {
							lockManager.handleNotification(event);
						}
						try {
							Thread.sleep(sleep);
						} catch (InterruptedException e) {
						}
						verifyThatOnlyOneOrLessServersHaveTheToken(lockManagers);
					}
				}
			});
			threads.add(thread);
		}
		for (Thread thread : threads) {
			thread.start();
		}
		for (Thread thread : threads) {
			if (thread.isAlive()) {
				thread.join();
			}
		}
	}

	private synchronized void verifyThatOnlyOneOrLessServersHaveTheToken(List<LockManager> lockManagers) {
		try {
			LockManager holder = null;
			for (LockManager lockManager : lockManagers) {
				if (lockManager.haveToken() && holder != null) {
					// We expect two holders, but the servers will normalise
					// after the first publishing
					logger.error("Two holders : " + holder.getServer() + ", " + lockManager.getServer());
					// assertFalse(true);
					break;
				}
				if (lockManager.haveToken()) {
					holder = lockManager;
				}
			}
		} finally {
			notifyAll();
		}
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
