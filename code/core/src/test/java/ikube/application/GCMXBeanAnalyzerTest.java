package ikube.application;

import ikube.AbstractTest;
import ikube.toolkit.ThreadUtilities;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Future;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
@Ignore
public class GCMXBeanAnalyzerTest extends AbstractTest {

    @Test
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    public void analyze() {
        final GCMXBeanAnalyzer gcmxBeanAnalyzer = new GCMXBeanAnalyzer();
        ThreadUtilities.submit("gc-analyzer", new Runnable() {
            public void run() {
                gcmxBeanAnalyzer.analyze();
            }
        });
        List<Future<Object>> futures = populateMemory(100);
        ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
        ThreadUtilities.sleep(5000);
        LinkedList<GCMXBeanAnalyzer.GCSnapshot> gcSnapshots = gcmxBeanAnalyzer.gcSnapshots;
        for (final GCMXBeanAnalyzer.GCSnapshot gcSnapshot : gcSnapshots) {
            logger.error(gcSnapshot.interval + ":" + gcSnapshot.duration + ":" + gcSnapshot.delta + ":" + gcSnapshot.available);
        }
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<Future<Object>> populateMemory(int threads) {
        List<Future<Object>> futures = new ArrayList<>();
        final List<Object> objects = Collections.synchronizedList(new ArrayList<Object>());
        while (threads-- >= 0) {
            Future future = ThreadUtilities.submit("memory-populate", new Runnable() {
                public void run() {
                    int retry = 10;
                    Random random = new Random();
                    //noinspection InfiniteLoopStatement
                    while (true) {
                        try {
                            //noinspection UnnecessaryBoxing
                            objects.add(new Double(random.nextDouble() * (Double.MAX_VALUE - 1)));
                            Thread.sleep(0, 1);
                        } catch (final OutOfMemoryError e) {
                            if (--retry <= 0) {
                                break;
                            }
                            objects.clear();
                            System.gc();
                            logger.error("Out of memory : ", e.getMessage());
                        } catch (final Exception e) {
                            // Ignore
                        }
                    }
                }
            });
            //noinspection unchecked
            futures.add(future);
        }
        return futures;
    }

}
