package ikube.application;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
class GCSmoother {

    Map<String, LinkedList<GCSnapshot>> getSmoothedSnapshots(final Map<String, LinkedList<GCSnapshot>> gcSnapshots) {
        Map<String, LinkedList<GCSnapshot>> smoothedGcSnapshots = new HashMap<>();
        // Smooth the snapshots, using one minute as the time interval
        // Calculate slope: y2 - y1 / x2 - x1
        // Calculate a point per minute: y = mx + c
        for (final Map.Entry<String, LinkedList<GCSnapshot>> mapEntry : gcSnapshots.entrySet()) {
            LinkedList<GCSnapshot> snapshots = mapEntry.getValue();
            LinkedList<GCSnapshot> smoothedSnapshots = getSmoothedSnapshots(snapshots);
            smoothedGcSnapshots.put(mapEntry.getKey(), smoothedSnapshots);
        }
        return smoothedGcSnapshots;
    }

    private LinkedList<GCSnapshot> getSmoothedSnapshots(final LinkedList<GCSnapshot> snapshots) {
        LinkedList<GCSnapshot> smoothedSnapshots = new LinkedList<>();
        // Average all the values per minute for the snapshots in the time range
        long startMinute = MILLISECONDS.toMinutes(snapshots.getFirst().start);
        int snapshotsPerMinute = 0;
        GCSnapshot smoothedGcSnapshot = new GCSnapshot();
        for (final GCSnapshot gcSnapshot : snapshots) {
            snapshotsPerMinute++;
            long gcMinute = MILLISECONDS.toMinutes(gcSnapshot.start);
            if (gcMinute > startMinute) {
                // Calculate the averages for the smoothed snapshot
                aggregateSnapshotValues(smoothedGcSnapshot, snapshotsPerMinute);
                smoothedSnapshots.add(smoothedGcSnapshot);

                // Reset the average running totals
                smoothedGcSnapshot = new GCSnapshot();
                snapshotsPerMinute = 1;
                startMinute = gcMinute;
            }
            accumulateSnapshotValues(gcSnapshot, smoothedGcSnapshot, startMinute);
        }
        return smoothedSnapshots;
    }

    private void aggregateSnapshotValues(final GCSnapshot smoothedGcSnapshot, final int snapshotsPerMinute) {
        smoothedGcSnapshot.duration = smoothedGcSnapshot.duration / snapshotsPerMinute;
        smoothedGcSnapshot.available = smoothedGcSnapshot.available / snapshotsPerMinute;
        smoothedGcSnapshot.usedToMaxRatio = smoothedGcSnapshot.usedToMaxRatio / snapshotsPerMinute;
        smoothedGcSnapshot.cpuLoad = smoothedGcSnapshot.cpuLoad / snapshotsPerMinute;
        smoothedGcSnapshot.processors = smoothedGcSnapshot.processors / snapshotsPerMinute;
        smoothedGcSnapshot.perCoreLoad = smoothedGcSnapshot.perCoreLoad / snapshotsPerMinute;
        smoothedGcSnapshot.threads = smoothedGcSnapshot.threads / snapshotsPerMinute;
        smoothedGcSnapshot.interval = smoothedGcSnapshot.interval / snapshotsPerMinute;
        smoothedGcSnapshot.delta = smoothedGcSnapshot.delta / snapshotsPerMinute;
        smoothedGcSnapshot.runsPerTimeUnit = smoothedGcSnapshot.runsPerTimeUnit / snapshotsPerMinute;
    }

    private void accumulateSnapshotValues(final GCSnapshot gcSnapshot, final GCSnapshot smoothedGcSnapshot, final long minute) {
        smoothedGcSnapshot.duration += gcSnapshot.duration;
        smoothedGcSnapshot.available += gcSnapshot.available;
        smoothedGcSnapshot.usedToMaxRatio += gcSnapshot.usedToMaxRatio;
        smoothedGcSnapshot.cpuLoad += gcSnapshot.cpuLoad;
        smoothedGcSnapshot.processors += gcSnapshot.processors;
        smoothedGcSnapshot.perCoreLoad += gcSnapshot.perCoreLoad;
        smoothedGcSnapshot.threads += gcSnapshot.threads;
        smoothedGcSnapshot.interval += gcSnapshot.interval;
        smoothedGcSnapshot.delta += gcSnapshot.delta;
        smoothedGcSnapshot.runsPerTimeUnit += gcSnapshot.runsPerTimeUnit;

        smoothedGcSnapshot.start = MINUTES.toMillis(minute);
        smoothedGcSnapshot.end = MINUTES.toMillis(minute + 1);
    }

}