package ikube.action.index.handler.filesystem;

import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableFileSystemLog;
import ikube.toolkit.FileUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides log files for the log file handler.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-08-2014
 */
public class LogFileResourceProvider implements IResourceProvider<File> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileResourceProvider.class);

    private List<File> resources;

    LogFileResourceProvider(final IndexableFileSystemLog indexableFileSystemLog) {
        String directoryPath = indexableFileSystemLog.getPath();
        File directory = FileUtilities.getFile(directoryPath, Boolean.TRUE);
        resources = new ArrayList<>();
        getLogFiles(directory, indexableFileSystemLog.getSuffix());
    }

    @Override
    public File getResource() {
        if (resources.isEmpty()) {
            return null;
        }
        return resources.remove(0);
    }

    @Override
    public void setResources(final List<File> resources) {
        this.resources = resources;
    }

    private void getLogFiles(final File directory, final String suffix) {
        File[] logFiles = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                // We need the directories of the log files below this point
                LOGGER.warn("Looking for logs files : " + pathname);
                return pathname.getName().endsWith(suffix) || pathname.isDirectory();
            }
        });
        if (logFiles != null) {
            for (final File logFile : logFiles) {
                if (logFile.isDirectory()) {
                    getLogFiles(logFile, suffix);
                    continue;
                }
                LOGGER.warn("Adding logs file : " + logFile);
                resources.add(logFile);
            }
        }
    }

}
