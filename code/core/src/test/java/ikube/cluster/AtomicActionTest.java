package ikube.cluster;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class AtomicActionTest extends ATest {

	public AtomicActionTest() {
		super(AtomicActionTest.class);
	}

	private IAtomicAction atomicAction = new IAtomicAction() {
		@Override
		@SuppressWarnings("unchecked")
		public <T> T execute() {
			return (T) Boolean.TRUE;
		}
	};

	@Test
	public void executeAction() {
		// This test needs to run in three threads at least
		int threadCount = 10;
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < threadCount; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						if (Math.random() < 0.1) {
							break;
						}
						boolean result = (Boolean) AtomicAction.executeAction(IConstants.SERVER_LOCK, atomicAction);
						logger.info("Result : " + result + ", " + this.hashCode());
						assertTrue("We should always get a result : ", result);
					}
				}
			});
			thread.start();
			threads.add(thread);
		}
		ThreadUtilities.waitForThreads(threads);
	}

}
