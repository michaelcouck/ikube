package ikube.application;

import java.io.Serializable;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
class GCSnapshot implements Serializable {

    /**
     * When the garbage collector started the mark sweep collection.
     */
    long start;
    /**
     * When the garbage collector ended the mark sweep collection.
     */
    long end;
    /**
     * How long the garbage collector took for the iteration.
     */
    long duration;

    /**
     * The interval between the garbage collections.
     */
    long interval;
    /**
     * The change in the memory for the particular memory segment, could be up or down.
     */
    double delta;
    /**
     * The amount of memory available after the collection, the maximum memory for the Jvm less the used portion.
     */
    double available;
    /**
     * The ratio of used memory to maximum.
     */
    double usedToMaxRatio;

    /**
     * The cpu load at the end of the collection. This is in units per core. For 8 cores it could be 5.32 for example
     * which will indicate quite a high load. As a rule of thumb the load should not exceed 1 point for each core, bearing in
     * mind that a large portion of the load could be system and or file system load and will not show in htop for example.
     */
    double cpuLoad;
    /**
     * The number of cores available to the Jvm. Also os specific, and in some cases not correct.
     */
    int processors;
    /**
     * The load per core, 1.6 for example means that the core is very heavily loaded, try to keep this
     * below one, or the instructions are piling up on the way in, reducing the efficiency of the core as the
     * os has to schedule packets into the core.
     */
    double perCoreLoad;
    /**
     * And the number of threads that are used in the Jvm at the end of the garbage collection.
     */
    int threads;

    /**
     * How many garbage collections are being performed per unit, could be a second or a minute.
     */
    int runsPerTimeUnit = 1;

}