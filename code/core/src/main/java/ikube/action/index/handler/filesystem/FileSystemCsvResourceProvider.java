package ikube.action.index.handler.filesystem;

import ikube.action.index.handler.IResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides the files for the csv handler, i.e. all the csv files in the path. Not this provider will
 * not walk the file system, but only look in the top level folder for files.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 08-02-2011
 */
public class FileSystemCsvResourceProvider implements IResourceProvider<File> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemCsvResourceProvider.class);

    private List<File> resources;

    FileSystemCsvResourceProvider(final String filePath) {
        LOGGER.info("Csv start path : " + filePath);
        File[] files = new File(filePath).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                boolean included = isIncluded(pathname);
                LOGGER.info("Included : " + included + ", " + pathname);
                return included;
            }
        });
        if (files != null) {
            setResources(new ArrayList<>(Arrays.asList(files)));
        }
    }

    @Override
    public synchronized File getResource() {
        if (resources == null || resources.isEmpty()) {
            return null;
        }
        return resources.remove(0);
    }

    @Override
    public void setResources(final List<File> resources) {
        this.resources = resources;
    }

    protected synchronized boolean isIncluded(final File file) {
        return file.getName().endsWith("csv");
    }
}
