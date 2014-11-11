package ikube.application;

import ikube.IntegrationTest;
import ikube.toolkit.ThreadUtilities;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import java.util.Arrays;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
@Ignore
public class GCAnalyzerIntegration extends IntegrationTest {

    @Spy
    @InjectMocks
    private GCAnalyzer gcAnalyzer;

    @Test
    public void registerCollector() throws Exception {
        String address = "localhost";
        gcAnalyzer.registerCollector(address, 8500);
        ThreadUtilities.sleep(1000 * 60 * 10);
        Object[][][] gcMatrices = gcAnalyzer.getGcData(address);
        logger.error("Matrices : " + Arrays.deepToString(gcMatrices));
    }

}
