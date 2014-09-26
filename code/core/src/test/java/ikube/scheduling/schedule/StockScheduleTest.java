package ikube.scheduling.schedule;

import ikube.AbstractTest;
import ikube.IConstants;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import java.io.File;

import static ikube.toolkit.FileUtilities.findFileRecursively;
import static ikube.toolkit.FileUtilities.getContent;
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
    private StockSchedule actionSchedule;

    @Test
    @SuppressWarnings("rawtypes")
    public void run() {
        actionSchedule.run();
        File file = findFileRecursively(new File(IConstants.ANALYTICS_DIRECTORY), "AHII.csv");
        assertNotNull(file);
        String content = getContent(file);
        assertTrue(StringUtils.isNotEmpty(content));
    }

}