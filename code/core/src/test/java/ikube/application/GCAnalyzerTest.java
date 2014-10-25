package ikube.application;

import ikube.AbstractTest;
import junit.framework.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import java.util.Date;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
public class GCAnalyzerTest extends AbstractTest {

    @Spy
    @InjectMocks
    private GCAnalyzer gcAnalyzer;

    @Test
    public void registerCollector() throws Exception {
        try {
            gcAnalyzer.registerCollector(null);
        } catch (final Exception e) {
            logger.error(null, e);
        }
    }

    @Test
    public void checkTimeUnitConversion() {
        // Check whether the time unit conversion rounds the time unit
        // on overflow, or truncates the excess seconds for the minute to
        // the lower bound
        for (int i = 0; i < 90; i++) {
            long nextSecond = currentTimeMillis() + (i * 1000);
            Date current = new Date(nextSecond);
            long millisToSeconds = MILLISECONDS.toSeconds(nextSecond);
            long millisToMinutes = MILLISECONDS.toMinutes(nextSecond);
            long minutesToMillis = MINUTES.toMillis(millisToMinutes);

            Date minutesToMillisDate = new Date(minutesToMillis);

            logger.debug(current.toString());
            logger.debug(Long.toString(millisToSeconds));
            logger.debug(Long.toString(millisToMinutes));
            logger.debug(minutesToMillisDate.toString());
            logger.debug("");

            long excessSeconds = millisToSeconds % 60l;
            long truncatedSeconds = millisToSeconds - excessSeconds;
            long truncatedMillis = truncatedSeconds * 1000;
            // And the time is truncated in the TimeUnit enumeration logic
            Assert.assertTrue(new Date(truncatedMillis).equals(minutesToMillisDate));
        }
    }

}