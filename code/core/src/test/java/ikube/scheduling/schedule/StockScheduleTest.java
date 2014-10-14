package ikube.scheduling.schedule;

import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 26-09-2014
 */
public class StockScheduleTest extends AbstractTest {

    @Spy
    @InjectMocks
    private StockSchedule stockSchedule;

    @Test
    public void getCompanySymbols() {
        String[] companySymbols = stockSchedule.getCompanySymbols();
        assertNotNull(companySymbols);
        assertTrue(companySymbols.length > 10);
    }

    @Test
    public void getHistoricalStockData() {
        String[] parameterNames = stockSchedule.parameterNames;
        String[] parameterValues = stockSchedule.getParameterValues(parameterNames);
        stockSchedule.getHistoricalStockData(parameterNames, parameterValues, "AHII");
        File ahiiStockFile = FileUtilities.findFileRecursively(new File("."), "AHII.csv");
        assertNotNull(ahiiStockFile);
        assertTrue(ahiiStockFile.exists());
    }

}