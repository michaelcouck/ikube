package ikube.application;

import ikube.toolkit.SerializationUtilities;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * This class will take the garbage collector snapshots and aggregate the values for the duration, the available
 * memory over a period(one minute at the time of writing). So for example if there are 28 garbage collections over
 * a particular minute, the average memory will be calculated for the time period and the value used to populate
 * a smoothed/averaged {@link ikube.application.GCSnapshot} object. These smoothed snapshots will then be returned
 * to the caller as a chronological list of averaged minutes.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
class GCSmoother {

    /**
     * This method aggregates the snapshots over a period of a minute. The original snapshots have the current
     * time in milliseconds as the starting value, but the smoothed snapshots have the minute as the starting value,
     * as the averages are over a minute and not milliseconds.
     *
     * @param gcSnapshots the snapshots that need to be averaged over a minute
     * @return the smoothed or averaged snapshots, typically there will be far less than the input, and note that
     * the start value is the minute and not the millisecond
     */
    @SuppressWarnings("unchecked")
    List<GCSnapshot> getSmoothedSnapshots(final List<GCSnapshot> gcSnapshots) {
        List<GCSnapshot> smoothedSnapshots = new LinkedList<>();
        if (gcSnapshots.isEmpty()) {
            return smoothedSnapshots;
        }

        GCSnapshot gcSnapshotSmooth = null;
        Iterator<GCSnapshot> gcSnapshotIterator = gcSnapshots.iterator();
        do {
            GCSnapshot gcSnapshot = gcSnapshotIterator.next();
            long nextMinute = MILLISECONDS.toMinutes(gcSnapshot.start);

            if (gcSnapshotSmooth == null) {
                gcSnapshotSmooth = SerializationUtilities.clone(GCSnapshot.class, gcSnapshot);
                gcSnapshotSmooth.start = nextMinute;
                gcSnapshotSmooth.end = nextMinute + 1;
                smoothedSnapshots.add(gcSnapshotSmooth);
                continue;
            }

            if (nextMinute > gcSnapshotSmooth.start) {

                aggregateSnapshotValues(gcSnapshotSmooth);
                gcSnapshotSmooth = new GCSnapshot();
                gcSnapshotSmooth.start = nextMinute;
                gcSnapshotSmooth.end = nextMinute + 1;

                smoothedSnapshots.add(gcSnapshotSmooth);
            }
            accumulateSnapshotValues(gcSnapshot, gcSnapshotSmooth);
            if (!gcSnapshotIterator.hasNext()) {
                aggregateSnapshotValues(gcSnapshotSmooth);
                break;
            }
        } while (true);

        return smoothedSnapshots;
    }

    private void aggregateSnapshotValues(final GCSnapshot smoothedGcSnapshot) {
        smoothedGcSnapshot.duration = smoothedGcSnapshot.duration / smoothedGcSnapshot.runsPerTimeUnit;
        smoothedGcSnapshot.available = smoothedGcSnapshot.available / smoothedGcSnapshot.runsPerTimeUnit;
        smoothedGcSnapshot.usedToMaxRatio = smoothedGcSnapshot.usedToMaxRatio / smoothedGcSnapshot.runsPerTimeUnit;
        smoothedGcSnapshot.cpuLoad = smoothedGcSnapshot.cpuLoad / smoothedGcSnapshot.runsPerTimeUnit;
        smoothedGcSnapshot.processors = smoothedGcSnapshot.processors / smoothedGcSnapshot.runsPerTimeUnit;
        smoothedGcSnapshot.perCoreLoad = smoothedGcSnapshot.perCoreLoad / smoothedGcSnapshot.runsPerTimeUnit;
        smoothedGcSnapshot.threads = smoothedGcSnapshot.threads / smoothedGcSnapshot.runsPerTimeUnit;
        smoothedGcSnapshot.interval = smoothedGcSnapshot.interval / smoothedGcSnapshot.runsPerTimeUnit;
        smoothedGcSnapshot.delta = smoothedGcSnapshot.delta / smoothedGcSnapshot.runsPerTimeUnit;
    }

    private void accumulateSnapshotValues(final GCSnapshot gcSnapshot, final GCSnapshot smoothedGcSnapshot) {
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
    }

}