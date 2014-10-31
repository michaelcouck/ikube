package ikube.application;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
class GCSnapshot {
    long start;
    long end;
    long duration;

    long interval;
    double delta;
    double available;
    double usedToMaxRatio;

    int runsPerTimeUnit = 1;
    double cpuLoad;
    int processors;
    double perCoreLoad;
    int threads;
}