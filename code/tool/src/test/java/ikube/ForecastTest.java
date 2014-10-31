package ikube;

import ikube.example.Forecast;
import ikube.toolkit.FileUtilities;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29-09-2014
 */
public class ForecastTest extends AbstractTest {

    @Test
    public void main() throws IOException {
        String inputFilePath = FileUtilities.findFileAndGetCleanedPath(new File("."), "forecast.csv");
        Forecast.main(new String[]{inputFilePath});
    }
}
