package ikube.cluster;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.cluster.ClusterManagerJms.Lock;
import ikube.toolkit.ThreadUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jms.ObjectMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ClusterManagerJmsTest extends ATest {

	private int iterations = 1000;
	private int clusterManagersSize = 10;

	private boolean wait;
	private boolean[] locks;
	private boolean[] unlocks;
	private List<ClusterManagerJms> clusterManagers;

	public ClusterManagerJmsTest() {
		super(ClusterManagerJmsTest.class);
	}

	@Before
	public void before() throws Exception {
		locks = new boolean[clusterManagersSize];
		unlocks = new boolean[clusterManagersSize];
		clusterManagers = new ArrayList<ClusterManagerJms>();
	}

	@Test
	public void integration() throws Exception {
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < clusterManagersSize; i++) {
			final int index = i;
			Thread thread = new Thread(new Runnable() {
				public void run() {
					ClusterManagerJms clusterManagerJms = getClusterManagerJms();
					sleepRandom();
					for (int i = 0; i < iterations; i++) {
						sleepRandom();
						if (Math.random() < 0.5) {
							boolean lock = clusterManagerJms.lock(null);
							locks[index] = lock;
						} else {
							boolean unlocked = clusterManagerJms.unlock(null);
							unlocks[index] = unlocked;
						}
						if (wait) {
							waitForRestart(1000);
						}
					}
				}
			}, Integer.toString(i));
			threads.add(thread);
		}
		sleepRandom();
		for (Thread thread : threads) {
			sleepRandom();
			thread.start();
		}
		Thread verifier = new Thread(new Runnable() {
			public void run() {
				while (true) {
					wait = Boolean.TRUE;
					waitForRestart(1000);
					// Check that there is only one that is locking the cluster
					for (int i = 0; i < clusterManagers.size(); i++) {
						ClusterManagerJms clusterManagerJms = clusterManagers.get(i);
						Iterator<Lock> iterator = clusterManagerJms.locks.values().iterator();
						int count = 0;
						for (int j = 0; iterator.hasNext(); j++) {
							Lock lock = iterator.next();
							if (lock.lock) {
								count++;
							}
						}
						if (count > 1) {
							logger.error("There can be only one cluster manager with the lock : ");
							logger.error(clusterManagerJms.locks);
							System.exit(1);
						}
					}
					wait = Boolean.FALSE;
					// Check that there is only one lock and one unlock in the arrays
					waitForRestart(3000);
				}
			}
		});
		verifier.setDaemon(Boolean.TRUE);
		verifier.start();
		ThreadUtilities.waitForThreads(threads);
	}

	private ClusterManagerJms getClusterManagerJms() {
		ClusterManagerJms clusterManagerJms = null;
		try {
			clusterManagerJms = new ClusterManagerJms();
		} catch (Exception e) {
			logger.error("", e);
		}
		ClusterManagerJms clusterManagerJmsSpy = spy(clusterManagerJms);
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				ObjectMessage objectMessage = mock(ObjectMessage.class);
				when(objectMessage.getObject()).thenReturn((Serializable) arguments[0]);
				for (ClusterManagerJms clusterManagerJms : clusterManagers) {
					clusterManagerJms.onMessage(objectMessage);
				}
				return null;
			}
		}).when(clusterManagerJmsSpy).sendLock(any(Lock.class));
		clusterManagers.add(clusterManagerJmsSpy);
		return clusterManagerJmsSpy;
	}

	private void waitForRestart(long sleep) {
		try {
			Thread.sleep(sleep);
		} catch (Exception e) {
		}
	}

	private void sleepRandom() {
		try {
			Thread.sleep((long) (Math.random() * 1000d));
		} catch (InterruptedException e) {
		}
	}

}