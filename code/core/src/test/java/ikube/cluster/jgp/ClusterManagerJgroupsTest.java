package ikube.cluster.jgp;

import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterManagerJgroupsTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private ClusterManagerJgroups clusterManagerJgroups;

	@Before
	public void before() throws Exception {
		clusterManagerJgroups = new ClusterManagerJgroups();
		clusterManagerJgroups.initialize();
	}

	@Test
	public void threaded() {
		ThreadUtilities.initialize();
		int threads = 3;
		final int iterations = 10000;
		final double sleep = 1000;
		final Boolean[] locks = new Boolean[threads];
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (int i = 0; i < threads; i++) {
			final int thread = i;
			Runnable runnable = new Runnable() {
				public void run() {
					for (int i = 0; i < iterations; i++) {
						boolean lock = clusterManagerJgroups.lock(IConstants.IKUBE);
						locks[thread] = lock;
						validate(locks);
						clusterManagerJgroups.unlock(IConstants.IKUBE);
						locks[thread] = false;
						ThreadUtilities.sleep((long) (sleep * Math.random()));
					}
				}
			};
			Future<?> future = ThreadUtilities.submit(runnable);
			futures.add(future);
		}
		ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
		ThreadUtilities.destroy();
	}

	private void validate(final Boolean[] locks) {
		int count = 0;
		logger.info("Validate ; " + Arrays.deepToString(locks));
		for (Boolean lock : locks) {
			count = lock == null ? count : lock ? count + 1 : count;
		}
		assertTrue(count <= 1);
	}

}
