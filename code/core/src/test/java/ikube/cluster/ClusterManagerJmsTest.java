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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.jms.ObjectMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ClusterManagerJmsTest extends ATest {

	private int iterations = 100;
	private int clusterManagersSize = 3;
	private List<ClusterManagerJms> clusterManagers;

	public ClusterManagerJmsTest() {
		super(ClusterManagerJmsTest.class);
	}

	@Before
	public void before() throws Exception {
		clusterManagers = new ArrayList<ClusterManagerJms>();
	}

	@Test
	public void integration() throws Exception {
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < clusterManagersSize; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					ClusterManagerJms clusterManagerJms = getClusterManagerJms();
					sleepRandom();
					for (int i = 0; i < iterations; i++) {
						sleepRandom();
						if (Math.random() < 0.5) {
							clusterManagerJms.lock(null);
							// logger.info("Lock : " + Thread.currentThread().hashCode() + " : " + clusterManagerJms.lock(null));
						} else {
							clusterManagerJms.unlock(null);
							// logger.info("Unlock : " + Thread.currentThread().hashCode() + " : " + clusterManagerJms.unlock(null));
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
		ThreadUtilities.waitForThreads(threads);
	}

	private ClusterManagerJms getClusterManagerJms() {
		ClusterManagerJms clusterManagerJms = null;
		try {
			clusterManagerJms = new ClusterManagerJms();
		} catch (UnknownHostException e) {
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

	private void sleepRandom() {
		try {
			Thread.sleep((long) (Math.random() * 1000d));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}