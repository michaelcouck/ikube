package ikube.analytics.weka;

import org.junit.Test;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-09-2014
 */
public class WekaTimeSeriesClassifierTest {

    @Test
    public void predictTimeSeries() throws Exception {
        new WekaTimeSeriesClassifier().predictTimeSeries();
    }

}
