package ikube.cluster;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.IConstants;
import ikube.cluster.jms.ClusterManagerJms;
import ikube.toolkit.ThreadUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.jms.ObjectMessage;

import mockit.Deencapsulation;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * This test is for the locking of the cluster. It will start several threads that will lock and unlock the cluster in a random fashion and
 * there should never be more than one thread that has the lock at any given time.
 * 
 * @author Michael Couck
 * @since 19.11.11
 * @version 01.00
 */
public class ClusterManagerJmsTest extends ATest {

	private int clubSize = 10;
	private long timeSpent = 60 * 10000000;
	private Random random = new Random();
	private boolean[] locks = new boolean[clubSize];
	private boolean[] unlocks = new boolean[clubSize];
	private boolean success = Boolean.TRUE;

	/**
	 * This class will execute locking and unlocking on the cluster manager in the constructor. It will also check the locks for the
	 * cluster, that there is only one at any particular time.
	 * 
	 * @author Michael Couck
	 * @since 02.01.12
	 * @version 01.00
	 */
	class Runner implements Runnable {

		/** The index in the boolean array of cluster locks that this runner is. */
		int index;
		/** The cluster manager to execute the locking and unlocking on. */
		ClusterManagerJms clusterManagerJms;

		Runner(int index, ClusterManagerJms clusterManagerJms) {
			this.index = index;
			this.clusterManagerJms = clusterManagerJms;
		}

		public void run() {
			try {
				long start = System.currentTimeMillis();
				while (System.currentTimeMillis() - start < timeSpent) {
					boolean bool = random.nextBoolean();
					if (bool) {
						locks[index] = clusterManagerJms.lock(IConstants.ID_LOCK);
					} else {
						unlocks[index] = clusterManagerJms.unlock(IConstants.ID_LOCK);
					}
					ThreadUtilities.sleep((long) (random.nextDouble() * 3d));
					boolean moreThanOneLock = containsMoreThanOne(locks);
					boolean moreThanOneUnlock = containsMoreThanOne(unlocks);
					if (moreThanOneLock) {
						logger.info("Locked : " + Arrays.toString(locks) + ", unlocked : " + Arrays.toString(unlocks));
						logger.info("More than one lock : " + moreThanOneLock);
						logger.info("More than one unlock : " + moreThanOneUnlock);
						// System.exit(0);
						success = Boolean.FALSE;
						ThreadUtilities.destroy();
						return;
					}
				}
			} catch (Exception e) {
				logger.error("Exception locking and unlocking the cluster : " + this, e);
				success = Boolean.FALSE;
				return;
			}
		}

		private boolean containsMoreThanOne(final boolean[] array) {
			int count = 0;
			for (boolean bool : array) {
				count += bool ? 1 : 0;
			}
			return count > 1;
		}
	}

	public ClusterManagerJmsTest() {
		super(ClusterManagerJmsTest.class);
	}

	@Test
	public void clusterSynchronisation() throws Exception {
		ArrayList<Thread> threads = new ArrayList<Thread>();
		ArrayList<ClusterManagerJms> clusterManagerJmsClub = getClusterManagerJmsClub(clubSize);
		for (int i = 0; i < clusterManagerJmsClub.size(); i++) {
			ClusterManagerJms clusterManagerJms = clusterManagerJmsClub.get(i);
			Thread thread = new Thread(new Runner(i, clusterManagerJms));
			threads.add(thread);
			thread.start();
		}
		ThreadUtilities.waitForThreads(threads);
		assertTrue("More than one lock : ", success);
	}

	private ArrayList<ClusterManagerJms> getClusterManagerJmsClub(final int size) throws Exception {
		final ArrayList<ClusterManagerJms> clusterManagerJmsClub = new ArrayList<ClusterManagerJms>();
		for (int i = 0; i < size; i++) {
			ClusterManagerJms clusterManagerJms = spy(new ClusterManagerJms());
			Deencapsulation.setField(clusterManagerJms, "jmsPort", new Long(System.nanoTime()));
			clusterManagerJms.initialize();
			doAnswer(new Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					// logger.info("Invocation : " + invocation);
					// Create the object message for transport
					ObjectMessage message = mock(ObjectMessage.class);
					Object[] parameters = invocation.getArguments();
					// This is the lock from the cluster manager
					Serializable serializable = (Serializable) parameters[0];
					when(message.getObject()).thenReturn(serializable);
					// Send to all members
					for (ClusterManagerJms clusterManagerJms : clusterManagerJmsClub) {
						clusterManagerJms.onMessage(message);
					}
					return null;
				}
			}).when(clusterManagerJms).sendMessage(any(Serializable.class));
			clusterManagerJmsClub.add(clusterManagerJms);
		}
		return clusterManagerJmsClub;
	}

	@Test
	public void haveLock() throws Exception {
		ClusterManagerJms clusterManagerJms = getClusterManagerJmsClub(1).iterator().next();
		boolean gotLock = clusterManagerJms.lock(null);
		assertTrue(gotLock);
		boolean unlocked = clusterManagerJms.unlock(null);
		assertTrue(unlocked);
	}

}