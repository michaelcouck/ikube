package ikube.application;

import ikube.analytics.IAnalyticsService;
import ikube.analytics.weka.WekaForecastClassifier;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
class GCAnalyzer implements Runnable {

    @Autowired
    IAnalyticsService analyticsService;

    Map<String, LinkedList<GCSnapshot>> gcSnapshots;

    GCAnalyzer(final Map<String, LinkedList<GCSnapshot>> gcSnapshots) {
        this.gcSnapshots = gcSnapshots;
    }

    @Override
    public void run() {
        // Smooth the snapshots, using one minute as the time interval
        // Calculate slope: y2 - y1 / x2 - x1
        // Calculate a point per minute: y = mx + c
        for (final LinkedList<GCSnapshot> snapshots : gcSnapshots.values()) {
            GCSnapshot previousGcSnapshot = null;
            for (final GCSnapshot gcSnapshot : snapshots) {
                if (previousGcSnapshot == null) {
                    previousGcSnapshot = gcSnapshot;
                    continue;
                }
                // Slope for the second
                gcSnapshot.slope =
                        (gcSnapshot.available - previousGcSnapshot.available) /
                                (MILLISECONDS.toSeconds((long) gcSnapshot.start) - MILLISECONDS.toSeconds((long) previousGcSnapshot.start));
            }
            // Average the slope per minute
            double minute = 0;
            for (final GCSnapshot gcSnapshot : snapshots) {
                if (minute == 0) {
                    minute = MILLISECONDS.toMinutes((long) gcSnapshot.start);
                }
            }
        }

        //noinspection InfiniteLoopStatement
        while (true) {
            ThreadUtilities.sleep(60000);

            Context context = new Context();
            context.setName(this.toString());
            context.setAnalyzer(WekaForecastClassifier.class.getName());
            context.setTrainingDatas("csv data");
            analyticsService.create(context);

            Analysis analysis = new Analysis();
            analysis.setContext(context.getName());
            //noinspection unchecked
            analysis.setInput("-fieldsToForecast,6,-timeStampField,0,-minLag,1,-maxLag,1,-forecasts,60");
            //noinspection unchecked
            analyticsService.analyze(analysis);

            // [[[579.3721684789788],[581.4060746802609],[583.233603088952],...]]
            analysis.getOutput();
        }
    }

}