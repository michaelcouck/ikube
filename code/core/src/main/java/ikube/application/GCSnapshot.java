package ikube.application;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
class GCSnapshot {
    double start;
    double end;
    double duration;

    double interval;
    double delta;
    double available;
    double usedToMaxRatio;

    double runsPerSecond = 1;
    double cpuLoad;
    double processors;
    double perCoreLoad;
    double threads;

    // Used in the calculations
    double slope;
}