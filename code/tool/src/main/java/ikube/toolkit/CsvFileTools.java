package ikube.toolkit;

import au.com.bytecode.opencsv.CSVWriter;
import ikube.IConstants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 30-07-2012
 */
public final class CsvFileTools {

    public static void main(final String[] args) {
        // "/home/laptop/Downloads/sentiment-analysis-dataset.csv"
        File input = new File(args[0]);
        // "/home/laptop/Workspace/ikube/code/libs/src/main/resources/indexes/bosal/"
        File outputDirectory = new File(args[1]);
        File output;
        Reader reader = null;
        Writer writer = null;
        int start = 0;
        int max = 150000;
        try {
            reader = new FileReader(input);
            BufferedReader bufferedReader = new BufferedReader(reader);
            // Skip the header
            bufferedReader.readLine();
            String line = bufferedReader.readLine();

            CSVWriter csvWriter = null;
            do {
                if (start % max == 0) {
                    output = new File(outputDirectory, "sentiment-model-" + start + "-" + (start + max) + ".arff");
                    writer = new FileWriter(output);
                    csvWriter = new CSVWriter(writer, ',', CSVWriter.NO_QUOTE_CHARACTER);
                    System.out.println("Counter : " + start);
                }
                start++;

                String[] inValues = StringUtils.split(line, IConstants.DELIMITER_CHARACTERS);
                String sentiment = inValues[1].equals("0") ? IConstants.NEGATIVE : IConstants.POSITIVE;
                String[] outValues = new String[]{sentiment, "'" + StringUtilities.stripToAlphaNumeric(inValues[3]) + "'"};
                //noinspection ConstantConditions
                csvWriter.writeNext(outValues);

            } while ((line = bufferedReader.readLine()) != null);
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(writer);
        }
    }

}
