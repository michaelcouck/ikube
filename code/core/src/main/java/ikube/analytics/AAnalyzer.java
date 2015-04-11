package ikube.analytics;

import ikube.model.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;

import static ikube.IConstants.ANALYTICS_DIRECTORY;
import static ikube.toolkit.ApplicationContextManager.getConfigFilePath;
import static ikube.toolkit.FILE.*;

/**
 * Common logic for analyzers.
 *
 * @author Michael Couck
 * @version 01.00
 * @see ikube.analytics.IAnalyzer
 * @since 11-09-2014
 */
public abstract class AAnalyzer<I, O, C> implements IAnalyzer<I, O, C> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * This method returns the data file for the analyzer. The data file contains the relational attribute set that can be
     * used to train the analyzer. As with all configuration, we expect this file to reside in the {@link ikube.IConstants#IKUBE_DIRECTORY}
     * some where.
     *
     * @param context the context for the analyzer that we want the input training file for
     * @return the data file for the analyzer, or null if no such file exists. Typically if the file is
     * not present then there is a serious problem and the analyzers will not work properly or at all, i.e.
     * the results are undefined
     */
    @SuppressWarnings("ConstantConditions")
    protected File[] getDataFiles(final Context context) {
        String configFilePath = cleanFilePath(getConfigFilePath());
        logger.info("Config file path : " + configFilePath);
        String[] dataFileNames = context.getFileNames();
        File[] dataFiles = context.getFileNames() != null ? new File[context.getFileNames().length] : null;
        File configurationDirectory = new File(configFilePath).getParentFile();
        for (int i = 0; dataFiles != null && i < dataFiles.length; i++) {
            String dataFileName = dataFileNames[i];
            // First look in the configuration directory specified in the system property
            File dataFile = findFileRecursively(configurationDirectory, dataFileName);
            logger.info("Looking for data file in directory : " + configurationDirectory.getAbsolutePath());
            // Then look starting from the dot directory
            if (dataFile == null || !dataFile.exists() || !dataFile.canRead()) {
                logger.info("Can't find data file : " + dataFileName + ", will search for it...");
                dataFile = findFileRecursively(new File("."), dataFileName);
            }
            // Create the file because we will need it
            if (dataFile == null || !dataFile.exists() || !dataFile.canRead()) {
                logger.warn("Couldn't find file for analyzer or can't read file, will create it : " + dataFileName);
                File analyticsDirectory = getOrCreateDirectory(ANALYTICS_DIRECTORY);
                dataFile = getOrCreateFile(new File(analyticsDirectory, dataFileName));
                logger.warn("Created data file : " + dataFile.getAbsolutePath());
            } else {
                logger.info("Found data file : " + dataFile.getAbsolutePath());
            }
            if (dataFile == null) {
                logger.warn("Couldn't create data file : " + dataFileName);
            }
            dataFiles[i] = dataFile;
        }
        logger.info("Data files : " + Arrays.toString(dataFiles));
        return dataFiles;
    }


}
