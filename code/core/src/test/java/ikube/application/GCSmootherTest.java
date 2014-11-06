package ikube.application;

import ikube.AbstractTest;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
public class GCSmootherTest extends AbstractTest {

    @Test
    public void getSmoothedSnapshots() {
        LinkedList<GCSnapshot> snapshots = new LinkedList<>();

        double seconds = 36000;
        final double maxAvailable = 16000d;
        GCSnapshot previousGcSnapshot = new GCSnapshot() {
            {
                this.available = maxAvailable;
            }
        };
        Random random = new Random();
        for (int i = 0; i < seconds; i++) {
            GCSnapshot gcSnapshot = new GCSnapshot();

            gcSnapshot.processors = 8;

            gcSnapshot.duration = random.nextInt(10000);
            gcSnapshot.available = previousGcSnapshot.available - (random.nextDouble() * 0.5d);
            gcSnapshot.usedToMaxRatio = (maxAvailable - gcSnapshot.available) / maxAvailable;
            gcSnapshot.cpuLoad = random.nextDouble() * gcSnapshot.processors;
            gcSnapshot.perCoreLoad = gcSnapshot.cpuLoad + gcSnapshot.processors;
            gcSnapshot.threads = 28;
            gcSnapshot.interval = random.nextInt(10000);
            gcSnapshot.delta = gcSnapshot.available - previousGcSnapshot.available;

            gcSnapshot.start = (long) (currentTimeMillis() + (i * 1000d));
            gcSnapshot.end = (long) (gcSnapshot.start + (random.nextDouble() * 10d));
            snapshots.add(gcSnapshot);

            assertTrue(previousGcSnapshot.available >= gcSnapshot.available);

            previousGcSnapshot = gcSnapshot;
        }

        GCSmoother gcSmoother = new GCSmoother();
        List<GCSnapshot> gcSnapshotsSmooth = gcSmoother.getSmoothedSnapshots(snapshots);
        Iterator<GCSnapshot> gcSnapshotIterator = gcSnapshotsSmooth.iterator();

        previousGcSnapshot = gcSnapshotIterator.next();
        while (gcSnapshotIterator.hasNext()) {
            GCSnapshot gcSnapshot = gcSnapshotIterator.next();

            assertTrue(previousGcSnapshot.available >= gcSnapshot.available);
            assertTrue(previousGcSnapshot.start <= gcSnapshot.start);

            previousGcSnapshot = gcSnapshot;
        }
    }

}