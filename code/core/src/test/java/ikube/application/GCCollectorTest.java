package ikube.application;

import ikube.AbstractTest;
import ikube.toolkit.ThreadUtilities;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
@Ignore
public class GCCollectorTest extends AbstractTest {

    @Test
    public void analyze() {
        ThreadUtilities.submit("gc-analyzer", new Runnable() {
            public void run() {
                new GCCollector();
            }
        });
        List<Future<Object>> futures = populateMemory(100);
        ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
        ThreadUtilities.sleep(5000);
    }

    private List<Future<Object>> populateMemory(int threads) {
        List<Future<Object>> futures = new ArrayList<>();
        final List<Object> objects = Collections.synchronizedList(new ArrayList<>());
        while (threads-- >= 0) {
            Future future = ThreadUtilities.submit("memory-populate", new Runnable() {
                public void run() {
                    int retry = 3;
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
