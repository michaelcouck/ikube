package ikube.toolkit;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 30-07-2012
 */
public class CsvFileTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvFileTools.class);

    public Object[][] getCsvData(final String inputFilePath) {
        try {
            File inputFile = new File(inputFilePath);
            return getCsvData(new FileInputStream(inputFile));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object[][] getCsvData(final InputStream inputStream, final int... excludedColumns) {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        try {
            CSVReader csvReader = new CSVReader(inputStreamReader);
            List<String[]> lines = csvReader.readAll();
            Object[][] matrix = new Object[lines.size()][];
            for (int i = 0; i < lines.size(); i++) {
                matrix[i] = excludeColumns(lines.get(i), excludedColumns);
            }
            return matrix;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public String[] excludeColumns(final String[] inputValues, final int... excludedColumns) {
        if (excludedColumns != null && excludedColumns.length > 0) {
            String[] strippedRow = new String[inputValues.length - excludedColumns.length];
            for (int j = 0, k = 0; j < inputValues.length; j++) {
                if (Arrays.binarySearch(excludedColumns, j) < 0) {
                    strippedRow[k++] = inputValues[j];
                }
            }
            return strippedRow;
        }
        return inputValues;
    }

}