package ikube.toolkit;

import au.com.bytecode.opencsv.CSVReader;
import ikube.AbstractTest;
import ikube.Constants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;

import java.io.File;
import java.io.FileReader;

import static ikube.toolkit.FileUtilities.findFileRecursively;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 16-08-2014
 */
public class CsvFileToolsTest extends AbstractTest {

    private String inputFilePath;

    @Before
    public void before() {
        File inputFile = FileUtilities.findFileRecursively(new File("."), "csv-file-tools.csv");
        inputFilePath = FileUtilities.cleanFilePath(inputFile.getAbsolutePath());
    }

    @Test
    public void removeColumns() throws Exception {
        File outputFile = FileUtilities.getOrCreateFile(new File("./target/csv-file-tools-cut.csv"));
        String outputFilePath = FileUtilities.cleanFilePath(outputFile.getAbsolutePath());

        String[] args = {"-i", inputFilePath, "-o", outputFilePath, "-c", "[1,3]"};

        CsvFileTools csvFileTools = new CsvFileTools();
        csvFileTools.doMain(args);
        csvFileTools.includeColumns();

        CSVReader csvReader = new CSVReader(new FileReader(outputFilePath));
        String[] values = csvReader.readNext();
        Assert.assertEquals(2, values.length);
    }

    @Test
    public void splitFile() throws CmdLineException {
        File outputFile = FileUtilities.getOrCreateDirectory(new File("./target"));
        String outputFilePath = FileUtilities.cleanFilePath(outputFile.getAbsolutePath());

        String[] args = {"-i", inputFilePath, "-o", outputFilePath};

        CsvFileTools csvFileTools = new CsvFileTools();
        csvFileTools.doMain(args);
        csvFileTools.splitFile();

        outputFile = findFileRecursively(new File("."), "sentiment-model-");
        assertNotNull(outputFile);
        assertTrue(FileUtilities.getContent(outputFile).contains(Constants.POSITIVE));
    }

    @Test
    public void getCsvData() throws Exception {
        String[] args = {"-i", inputFilePath};

        CsvFileTools csvFileTools = new CsvFileTools();
        csvFileTools.doMain(args);
        Object[][] csvData = csvFileTools.getCsvData();
        assertNotNull(csvData);
        assertTrue(csvData.length == 2);
        assertTrue(csvData[0].length == 4);
    }

}