package ikube.application;

import ikube.toolkit.SerializationUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(GCSmoother.class);

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
            if (gcSnapshotSmooth == null) {
                gcSnapshotSmooth = SerializationUtilities.clone(GCSnapshot.class, gcSnapshot);
                gcSnapshotSmooth.start = gcSnapshot.start - (gcSnapshot.start % 60000);
                gcSnapshotSmooth.end = gcSnapshotSmooth.start + 60000;
                continue;
            }
            if (gcSnapshot.start - (gcSnapshot.start % 60000) > gcSnapshotSmooth.start || !gcSnapshotIterator.hasNext()) {
                aggregateSnapshotValues(gcSnapshotSmooth);
                smoothedSnapshots.add(gcSnapshotSmooth);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("Start : " + new Date(gcSnapshotSmooth.start).toString());
                    LOGGER.error("End   : " + new Date(gcSnapshotSmooth.end).toString());
                }
                gcSnapshotSmooth = null;
                continue;
            }
            accumulateSnapshotValues(gcSnapshot, gcSnapshotSmooth);
        } while (gcSnapshotIterator.hasNext());

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