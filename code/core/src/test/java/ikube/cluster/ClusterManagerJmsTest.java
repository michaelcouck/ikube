package ikube.cluster;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
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
import org.junit.Ignore;
import org.junit.Test;

public class ClusterManagerJmsTest extends ATest {

	private int iterations = 100000;
	private int clusterManagersSize = 5;
	private long maxWait = 100;

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
	@Ignore
	public void haveLock() throws Exception {
		ClusterManagerJms clusterManagerJms = getClusterManagerJms();

		boolean gotLock = clusterManagerJms.lock(null);
		assertTrue(gotLock);

		Lock lock = clusterManagerJms.locks.get(clusterManagerJms.ip);
		boolean haveLock = clusterManagerJms.haveLock(lock.shout);
		assertTrue("We must have the lock : ", haveLock);

		boolean unlocked = clusterManagerJms.unlock(null);
		assertTrue(unlocked);
	}

	@Test
	public void integration() throws Exception {
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < clusterManagersSize; i++) {
			final int index = i;
			Thread thread = new Thread(new Runnable() {
				public void run() {
					ClusterManagerJms clusterManagerJms = getClusterManagerJms();
					sleepRandom(maxWait);
					for (int i = 0; i < iterations; i++) {
						sleepRandom(maxWait);
						if (Math.random() < (maxWait / 2)) {
							locks[index] = clusterManagerJms.lock(null);
						} else {
							unlocks[index] = clusterManagerJms.unlock(null);
						}
						if (wait) {
							waitForRestart(maxWait * 2);
						}
					}
				}
			}, "Cluster manager thread : " + hashCode());
			threads.add(thread);
			waitForRestart(maxWait);
		}
		for (Thread thread : threads) {
			thread.start();
		}
		Thread verifier = new Thread(new Runnable() {
			public void run() {
				while (true) {
					wait = Boolean.TRUE;
					waitForRestart(maxWait * 3);
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
							logger.error("There can be only one cluster manager with the lock : " + clusterManagerJms.locks.size());
							logger.error(clusterManagerJms.locks);
							System.exit(1);
						}
					}
					wait = Boolean.FALSE;
					// Check that there is only one lock and one unlock in the arrays
					waitForRestart(maxWait * 3);
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
			clusterManagerJms = new ClusterManagerJms() {
				public void sendMessage(Serializable serializable) {
					try {
						ObjectMessage objectMessage = mock(ObjectMessage.class);
						when(objectMessage.getObject()).thenReturn(serializable);
						for (ClusterManagerJms clusterManagerJms : clusterManagers) {
							clusterManagerJms.onMessage(objectMessage);
						}
					} catch (Exception e) {
						logger.error("Exception iterating over the cluster managers : ", e);
					}
				}
			};
		} catch (Exception e) {
			logger.error("", e);
		}
		// ClusterManagerJms clusterManagerJmsSpy = spy(clusterManagerJms);
		// doAnswer(new Answer<Object>() {
		// @Override
		// public Object answer(InvocationOnMock invocation) throws Throwable {
		// Object[] arguments = invocation.getArguments();
		// ObjectMessage objectMessage = mock(ObjectMessage.class);
		// when(objectMessage.getObject()).thenReturn((Serializable) arguments[0]);
		// try {
		// for (ClusterManagerJms clusterManagerJms : clusterManagers) {
		// clusterManagerJms.onMessage(objectMessage);
		// }
		// } catch (Exception e) {
		// logger.error("Exception iterating over the cluster managers : ", e);
		// }
		// return null;
		// }
		// }).when(clusterManagerJmsSpy).sendMessage(any(Lock.class));

		clusterManagers.add(clusterManagerJms);
		return clusterManagerJms;
	}

	private void waitForRestart(long sleep) {
		try {
			Thread.sleep(sleep);
		} catch (Exception e) {
		}
	}

	private void sleepRandom(double max) {
		try {
			Thread.sleep((long) (Math.random() * max));
		} catch (InterruptedException e) {
		}
	}

}