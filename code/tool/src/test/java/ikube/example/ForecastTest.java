package ikube.example;

import ikube.AbstractTest;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static ikube.toolkit.FILE.findFileAndGetCleanedPath;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29-09-2014
 */
public class ForecastTest extends AbstractTest {

    @Test
    public void doAnalysis() throws IOException {
        String inputFilePath = findFileAndGetCleanedPath(new File("."), "forecast.csv");

        Base.Analysis analysis = Forecast.doAnalysis(new String[]{inputFilePath});
        logger.debug("Analysis : " + ToStringBuilder.reflectionToString(analysis));
        assertTrue(analysis.output.toString().contains("579.3721"));
        assertTrue(analysis.output.toString().contains("581.4060"));
        assertTrue(analysis.output.toString().contains("584.8823"));
        assertTrue(analysis.output.toString().contains("586.3763"));
    }
}
