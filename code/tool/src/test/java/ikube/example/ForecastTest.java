package ikube.example;

import ikube.AbstractTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static ikube.toolkit.FileUtilities.findFileAndGetCleanedPath;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29-09-2014
 */
@Ignore
public class ForecastTest extends AbstractTest {

    @Test
    public void doAnalysis() throws IOException {
        String inputFilePath = findFileAndGetCleanedPath(new File("."), "forecast.csv");

        Base.Analysis analysis = Forecast.doAnalysis(new String[]{inputFilePath});
        assertTrue(analysis.output.toString().contains("579.3721684789788"));
        assertTrue(analysis.output.toString().contains("581.4060746802609"));
        assertTrue(analysis.output.toString().contains("584.8823713779697"));
        assertTrue(analysis.output.toString().contains("586.3763013969173"));
    }
}
