package ikube.application;

import java.io.Serializable;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
class GCSnapshot implements Serializable {

    long start;
    long end;
    long duration;

    long interval;
    double delta;
    double available;
    double usedToMaxRatio;

    double cpuLoad;
    int processors;
    double perCoreLoad;
    int threads;

    int runsPerTimeUnit = 1;

}