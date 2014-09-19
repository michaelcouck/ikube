package ikube.toolkit;

import au.com.bytecode.opencsv.CSVReader;
import ikube.AbstractTest;
import ikube.Constants;
import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;
import org.mockito.Spy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;

import static ikube.toolkit.FileUtilities.findFileRecursively;
import static org.junit.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 16-08-2014
 */
public class CsvFileToolsTest extends AbstractTest {

    private String inputFilePath;
    @Spy
    private CsvFileTools csvFileTools;

    @Before
    public void before() {
        File inputFile = FileUtilities.findFileRecursively(new File("."), "csv-file-tools.csv");
        inputFilePath = FileUtilities.cleanFilePath(inputFile.getAbsolutePath());
        Deencapsulation.setField(csvFileTools, "inputFile", inputFilePath);
    }

    @Test
    public void removeColumns() throws Exception {
        File outputFile = FileUtilities.getOrCreateFile(new File("./target/csv-file-tools-cut.csv"));
        String outputFilePath = FileUtilities.cleanFilePath(outputFile.getAbsolutePath());

        Deencapsulation.setField(csvFileTools, "outputFile", outputFilePath);
        Deencapsulation.setField(csvFileTools, "columnsToInclude", "[1,3]");

        csvFileTools.includeColumns();

        CSVReader csvReader = new CSVReader(new FileReader(outputFilePath));
        String[] values = csvReader.readNext();
        Assert.assertEquals(2, values.length);
    }

    @Test
    public void splitFile() throws CmdLineException {
        File outputFile = FileUtilities.getOrCreateDirectory(new File("./target"));
        String outputFilePath = FileUtilities.cleanFilePath(outputFile.getAbsolutePath());

        Deencapsulation.setField(csvFileTools, "outputFile", outputFilePath);

        csvFileTools.splitFile();

        outputFile = findFileRecursively(new File("."), "sentiment-model-");
        assertNotNull(outputFile);
        assertTrue(FileUtilities.getContent(outputFile).contains(Constants.POSITIVE));
    }

    @Test
    public void getCsvData() throws Exception {
        Object[][] csvData = csvFileTools.getCsvData();
        assertNotNull(csvData);
        assertTrue(csvData.length == 2);
        assertTrue(csvData[0].length == 4);
    }

    @Test
    public void getCsvDataStreamExcluded() throws Exception {
        String input = "1,2,3,4,5,6,7,8,9,10\n1,2,3,4,5,6,7,8,9,10\n1,2,3,4,5,6,7,8,9,10";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        Object[][] csvData = csvFileTools.getCsvData(inputStream, 2, 8, 9);
        assertEquals(3, csvData.length);
        assertEquals(7, csvData[0].length);
    }

}