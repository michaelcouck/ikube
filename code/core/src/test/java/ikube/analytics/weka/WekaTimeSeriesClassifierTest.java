package ikube.analytics.weka;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-09-2014
 */
@Ignore
public class WekaTimeSeriesClassifierTest {

    @Test
    public void predictTimeSeries() throws Exception {
        new WekaTimeSeriesClassifier().predictTimeSeries();
    }

}
