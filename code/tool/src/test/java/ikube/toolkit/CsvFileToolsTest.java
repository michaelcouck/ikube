package ikube.toolkit;

import ikube.AbstractTest;
import ikube.IConstants;
import org.junit.Test;

import java.io.File;

import static ikube.toolkit.FileUtilities.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 16-08-2014
 */
public class CsvFileToolsTest extends AbstractTest {

    @Test
    public void main() {
        File outputFile = null;
        try {
            File csvFile = findFileRecursively(new File("."), "csv-file-tools.csv");
            String filePath = cleanFilePath(csvFile.getAbsolutePath());
            CsvFileTools.main(new String[]{filePath, "./ikube"});
            outputFile = findFileRecursively(new File("."), "sentiment-model-");
            assertNotNull(outputFile);
            assertTrue(FileUtilities.getContent(outputFile).contains(IConstants.POSITIVE));
        } finally {
            deleteFile(outputFile);
        }
    }

}
