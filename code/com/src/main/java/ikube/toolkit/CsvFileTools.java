package ikube.toolkit;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import ikube.Constants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 30-07-2012
 */
public final class CsvFileTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvFileTools.class);

    public static void main(final String[] args) {
        try {
            new CsvFileTools().doMain(args);
        } catch (final CmdLineException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object[][] getCsvData(final String[] args) {
        try {
            return new CsvFileTools().doMain(args).getCsvData();
        } catch (final CmdLineException e) {
            throw new RuntimeException(e);
        }
    }

    @Option(name = "-i", usage = "The input file for processing, must be csv")
    private String inputFile;
    @Option(name = "-o", usage = "The path to the output file, or the base name of the output files")
    private String outputFile;
    @Option(name = "-c", usage = "The indexes of the columns to include from the file, starting at zero, must be in Json format, i.e. [2, 8, 9]")
    private String columnsToInclude;
    @Option(name = "-m", usage = "The method to invoke")
    private String method;

    CsvFileTools doMain(final String[] args) throws CmdLineException {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(140);
        parser.parseArgument(args);

        if (!StringUtils.isEmpty(method)) {
            final CsvFileTools csvFileTools = this;
            ReflectionUtils.doWithMethods(this.getClass(), new ReflectionUtils.MethodCallback() {
                @Override
                public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException {
                    if (method.getName().equals(CsvFileTools.this.method)) {
                        try {
                            method.invoke(csvFileTools);
                        } catch (final InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        return this;
    }

    Object[][] getCsvData() {
        File inputFile = new File(this.inputFile);
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(inputFile);
            CSVReader csvReader = new CSVReader(fileReader);
            List<String[]> lines = csvReader.readAll();
            Object[][] matrix = new Object[lines.size()][];
            for (int i = 0; i < lines.size(); i++) {
                String[] row = lines.get(i);
                matrix[i] = row;
            }
            return matrix;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(fileReader);
        }
    }

    void includeColumns() {
        Reader reader = null;
        Writer writer = null;

        try {
            reader = new FileReader(inputFile);
            CSVReader csvReader = new CSVReader(reader);

            File output = new File(outputFile);
            writer = new FileWriter(output);
            CSVWriter csvWriter = new CSVWriter(writer);

            int[] columnsToInclude = Constants.GSON.fromJson(this.columnsToInclude, int[].class);
            String[] inValues;
            while ((inValues = csvReader.readNext()) != null) {
                LOGGER.error("In values : " + Arrays.deepToString(inValues));
                String[] outValues = new String[columnsToInclude.length];
                for (int i = 0, index = 0; i < inValues.length; i++) {
                    boolean includeColumn = Arrays.binarySearch(columnsToInclude, i) >= 0;
                    // LOGGER.error("Looking for column : " + i + ", in : " + this.columnsToRemove + ", " + includeColumn);
                    if (includeColumn) {
                        if (index <= outValues.length - 1) {
                            outValues[index] = inValues[i];
                            index++;
                        }
                    }
                }
                LOGGER.error("Out values : " + Arrays.deepToString(outValues));
                csvWriter.writeNext(outValues);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(writer);
        }
    }

    void splitFile() {
        File output;
        Reader reader = null;
        Writer writer = null;
        int start = 0;
        int max = 150000;
        try {
            reader = new FileReader(inputFile);
            BufferedReader bufferedReader = new BufferedReader(reader);
            // Skip the header
            bufferedReader.readLine();
            String line = bufferedReader.readLine();

            CSVWriter csvWriter = null;
            do {
                if (start % max == 0) {
                    output = new File(outputFile, "sentiment-model-" + start + "-" + (start + max) + ".arff");
                    writer = new FileWriter(output);
                    csvWriter = new CSVWriter(writer, ',', CSVWriter.NO_QUOTE_CHARACTER);
                    System.out.println("Counter : " + start);
                }
                start++;

                String[] inValues = StringUtils.split(line, Constants.DELIMITER_CHARACTERS);
                String sentiment = inValues[1].equals("0") ? Constants.NEGATIVE : Constants.POSITIVE;
                String[] outValues = new String[]{sentiment, "'" + StringUtilities.stripToAlphaNumeric(inValues[3]) + "'"};
                //noinspection ConstantConditions
                csvWriter.writeNext(outValues);

            } while ((line = bufferedReader.readLine()) != null);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(writer);
        }
    }

}