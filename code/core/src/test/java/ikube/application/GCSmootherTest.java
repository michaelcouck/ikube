package ikube.application;

import ikube.AbstractTest;
import org.junit.Test;

import java.util.*;

import static java.lang.System.currentTimeMillis;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
public class GCSmootherTest extends AbstractTest {

    @Test
    public void getSmoothedSnapshots() {
        Map<String, LinkedList<GCSnapshot>> gcSnapshots = new HashMap<>();
        LinkedList<GCSnapshot> snapshots = new LinkedList<>();
        gcSnapshots.put(GCCollector.OLD_GEN, snapshots);

        double minutes = 3600;
        double previousAvailable = Long.MAX_VALUE;
        Random random = new Random();
        for (int i = 0; i < minutes; i++) {
            GCSnapshot gcSnapshot = new GCSnapshot();

            gcSnapshot.available = minutes - random.nextInt(Math.max(1, i));
            gcSnapshot.available = Math.min(previousAvailable, gcSnapshot.available);
            previousAvailable = gcSnapshot.available;
            gcSnapshot.available = gcSnapshot.available - random.nextInt(5);

            gcSnapshot.start = currentTimeMillis() + (i * 1000);
            gcSnapshot.end = gcSnapshot.start + 10;
            snapshots.add(gcSnapshot);
        }

        GCSmoother gcSmoother = new GCSmoother();
        Map<String, LinkedList<GCSnapshot>> smoothedGcSnapshots = gcSmoother.getSmoothedSnapshots(gcSnapshots);
        for (final LinkedList<GCSnapshot> smoothedSnapshots : smoothedGcSnapshots.values()) {
            for (final GCSnapshot snapshot : smoothedSnapshots) {
                logger.warn("Smoothed snapshot : " + new Date(snapshot.start) + ", " + snapshot.available);
            }
        }
    }

}