package ikube.use;

import ikube.toolkit.CsvFileTools;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 24-09-2014
 */
public class NetFlixDataProcess {

    NetFlixDataProcess() throws IOException {
        // End result - 1,2003,Dinosaur Planet,1488844,3,2005-09-06

        // Read in the movies file into a csv model
        // Read the rating files
        // Write out the output file according to the input data

        File outputFile = FileUtilities.getOrCreateFile(new File("code/tool/src/main/resources"));
        FileWriter outputFileWriter = new FileWriter(outputFile);

        File[] trainingFiles = FileUtilities.findFiles(new File("/home/laptop/Downloads/netflix/training_set"), "mv_00");
        Object[][] movieTitleData = new CsvFileTools().getCsvData("/home/laptop/Downloads/netflix/movie_titles.txt");
        for (final File trainingFile : trainingFiles) {
            // Object[][]
        }
    }

}
