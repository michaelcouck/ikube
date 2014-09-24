package ikube.use;

import ikube.toolkit.CsvUtilities;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.MatrixUtilities;
import ikube.toolkit.StringUtilities;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import static ikube.toolkit.CsvUtilities.getCsvData;
import static ikube.toolkit.FileUtilities.deleteFile;
import static ikube.toolkit.FileUtilities.getOrCreateFile;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 24-09-2014
 */
public class NetFlixDataProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetFlixDataProcess.class);

    @SuppressWarnings("ConstantConditions")
    NetFlixDataProcess() throws IOException {
        // End result - 1,2003,Dinosaur Planet,1488844,3,2005-09-06

        // Read in the movies file into a csv model
        // Read the rating files
        // Write out the output file according to the input data

        File outputFile = null;
        String outputFilePath = "/home/laptop/Downloads/netflix/processed-data/mv-aggregated-";

        File[] trainingFiles = FileUtilities.findFiles(new File("/home/laptop/Downloads/netflix/training_set"), "mv_00");
        Object[][] movieTitleData = getCsvData("/home/laptop/Downloads/netflix/movie_titles.txt");

        int ratingCounter = 0;
        for (final File trainingFile : trainingFiles) {

            if (ratingCounter == 0 || ratingCounter > 1000000) {
                // Create a new output file
                outputFile = new File(outputFilePath + System.currentTimeMillis() + ".csv");
                deleteFile(outputFile);
                outputFile = getOrCreateFile(outputFile);
                ratingCounter = 0;
                LOGGER.error("New file : " + outputFile);
            }

            FileInputStream fileInputStream = new FileInputStream(trainingFile);
            Object[][] ratingData = getCsvData(fileInputStream);
            int movieId;
            Object[] movieTitleVector = null;
            for (final Object[] ratingInstance : ratingData) {
                if (ratingInstance == null || ratingInstance.length == 0) {
                    continue;
                }
                if (ratingInstance.length == 1) {
                    // This is the movie id
                    String strippedMovieId = StringUtilities.strip(ratingInstance[0].toString(), ":");
                    strippedMovieId = StringUtils.stripToEmpty(strippedMovieId);
                    movieId = Integer.parseInt(strippedMovieId);
                    // Find the movie title for the id
                    movieTitleVector = findMovieTitleVector(movieId, movieTitleData);

                    if (movieTitleVector.length > 3) {
                        LOGGER.error("Too long : " + Arrays.toString(movieTitleVector));
                        String[] movieTitleArray = Arrays.copyOfRange(movieTitleVector, 2, movieTitleVector.length, String[].class);
                        String movieTitle = StringUtils.join(movieTitleArray);
                        movieTitleVector[2] = movieTitle;
                        movieTitleVector = Arrays.copyOfRange(movieTitleVector, 0, 3, Object[].class);
                    }
                    continue;
                }
                // Create the vector to output
                Object[] outputVector = new Object[6];
                System.arraycopy(movieTitleVector, 0, outputVector, 0, movieTitleVector.length);
                System.arraycopy(ratingInstance, 0, outputVector, movieTitleVector.length, ratingInstance.length);
                // Write the data out
                String[] outputValues = MatrixUtilities.objectVectorToStringVector(outputVector);
                CsvUtilities.setCsvData(outputValues, outputFile, Boolean.TRUE);
                ratingCounter++;
            }
        }
    }

    private Object[] findMovieTitleVector(final int key, final Object[][] movieTitleData) {
        int low = 0;
        int high = movieTitleData.length - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = Integer.parseInt(movieTitleData[mid][0].toString());
            int cmp = midVal < key ? -1 : key == midVal ? 0 : 1;
            // LOGGER.error("Key : " + key + ", value : " + movieTitleData[mid][0].toString());
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return movieTitleData[mid]; // key found
        }
        return null;  // key not found.
    }

}