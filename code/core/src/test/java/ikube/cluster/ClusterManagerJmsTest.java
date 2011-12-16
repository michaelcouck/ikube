package ikube.cluster;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.cluster.jms.ClusterManagerJms;
import ikube.toolkit.ThreadUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.ObjectMessage;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test is for the locking of the cluster. It will start several threads that will lock and unlock the cluster in a random fashion and
 * there should never be more than one thread that has the lock at any given time.
 * 
 * @author Michael Couck
 * @since 19.11.11
 * @version 01.00
 */
public class ClusterManagerJmsTest extends ATest {

	private long maxWait = 100;
	private int iterations = 100;
	private int clusterManagersSize = 25;

	private boolean wait;
	private List<ClusterManagerJms> clusterManagers;
	private Map<ClusterManagerJms, Boolean> locks;
	private Map<ClusterManagerJms, Boolean> unlocks;

	public ClusterManagerJmsTest() {
		super(ClusterManagerJmsTest.class);
	}

	@Before
	public void before() throws Exception {
		clusterManagers = new ArrayList<ClusterManagerJms>();
		locks = new HashMap<ClusterManagerJms, Boolean>();
		unlocks = new HashMap<ClusterManagerJms, Boolean>();
	}

	/** TODO RE_DO THIS TEST! */
	@Test
	@Ignore
	public void clusterSynchronisation() throws Exception {
		startVerifier();
		List<Thread> threads = new ArrayList<Thread>();
		// Lock and unlock the cluster in threads to simulate a cluster
		for (int i = 0; i < clusterManagersSize; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					ClusterManagerJms clusterManagerJms = getClusterManagerJms();
					sleepRandom(maxWait);
					for (int i = 0; i < iterations; i++) {
						sleepRandom(maxWait);
						try {
							if (Math.random() < 0.5) {
								locks.put(clusterManagerJms, clusterManagerJms.lock(null));
							} else {
								unlocks.put(clusterManagerJms, clusterManagerJms.unlock(null));
							}
							if (wait) {
								sleepForAWhile(maxWait * 2);
							}
						} catch (Exception e) {
							logger.error("Exception locking or unlocking : ", e);
						}
					}
				}
			}, "Cluster manager thread : " + hashCode());
			threads.add(thread);
			// sleepForAWhile(1000);
		}
		for (Thread thread : threads) {
			sleepForAWhile(1000);
			thread.start();
		}
		ThreadUtilities.waitForThreads(threads);
	}

	/**
	 * This method starts a thread that will then verify that there are never more that one servers that have the lock for the cluster at
	 * any time.
	 */
	private void startVerifier() {
		Thread verifier = new Thread(new Runnable() {
			public void run() {
				while (true) {
					wait = Boolean.TRUE;
					sleepForAWhile(maxWait * 3);
					logger.info("Locks : " + locks.values().contains(Boolean.TRUE) + ":" + locks.values());
					wait = Boolean.FALSE;
					// Check that there is only one lock and one unlock in the arrays
					if (moreThanOneLock(locks.values())/* || moreThanOneLock(unlocks.values()) */) {
						logger.error("Only one server can have the lock or unlock : " + locks);
						// System.exit(0);
					}
					sleepForAWhile(maxWait * 3);
				}
			}
		});
		verifier.setDaemon(Boolean.TRUE);
		verifier.start();
	}

	private boolean moreThanOneLock(Collection<Boolean> collection) {
		int count = 0;
		for (Boolean bool : collection) {
			if (bool) {
				count++;
			}
		}
		return count > 1;
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
			clusterManagerJms.initialize();
		} catch (Exception e) {
			logger.error("", e);
		}
		clusterManagers.add(clusterManagerJms);
		return clusterManagerJms;
	}

	private void sleepRandom(double max) {
		sleepForAWhile((long) (Math.random() * max));
	}

	private void sleepForAWhile(long sleep) {
		try {
			Thread.sleep(sleep);
		} catch (Exception e) {
			logger.error("Exception sleeping : ", e);
		}
	}

	@Test
	public void haveLock() throws Exception {
		IClusterManager clusterManagerJms = getClusterManagerJms();
		boolean gotLock = clusterManagerJms.lock(null);
		assertTrue(gotLock);
		boolean unlocked = clusterManagerJms.unlock(null);
		assertTrue(unlocked);
	}

}