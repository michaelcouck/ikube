package ikube.action.index.handler.filesystem;

import ikube.IConstants;
import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * This class will provide files to the {@link IndexableFileSystemHandler}. It simply walks the file system from the starting
 * point that is specified, ignoring the files and paths that were specified as excluded, adds the files to the stack and makes
 * them available to the handler. The walk of the file system will pause if the size of the stack is too large, i.e. if the
 * consumers cannot keep pace with the file system walking.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 25.03.13
 */
class FileSystemResourceProvider implements IResourceProvider<File> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemResourceProvider.class);
    private final Stack<File> includedFiles;
    private final Stack<File> excludedFiles;
    private boolean finished = Boolean.FALSE;
    private boolean terminated;

    /**
     * The constructor takes the configuration for the walk of the file system, and start a thread that will do the actual walk,
     * so the caller does not have to wait for the walk to finish before starting the consumption of the files.
     *
     * @param indexableFileSystem the configuration of the file system walk
     * @param pattern             the pattern to exclude from the files accumulated
     * @throws IOException
     */
    FileSystemResourceProvider(final IndexableFileSystem indexableFileSystem, final Pattern pattern) throws IOException {
        includedFiles = new Stack<>();
        excludedFiles = new Stack<>();
        final File startDirectory = new File(indexableFileSystem.getPath());
        LOGGER.info("Start directory :" + startDirectory);
        THREAD.submit(this.toString(), new Runnable() {

            /**
             * {@inheritDoc}
             */
            public void run() {
                walkFileSystem(startDirectory);
                finished = Boolean.TRUE;
                LOGGER.info("Finished : " + true +
                        ", included : " + includedFiles.size() +
                        ", excluded : " + excludedFiles.size());
            }

            private void walkFileSystem(final File file) {
                if (isTerminated()) {
                    return;
                }
                try {
                    while (includedFiles.size() > IConstants.MILLION) {
                        THREAD.sleep(10000);
                    }
                    if (FILE.isExcluded(file, pattern)) {
                        excludedFiles.push(file);
                        return;
                    }
                    if (file.isDirectory()) {
                        File[] files = file.listFiles();
                        if (files != null && files.length > 0) {
                            for (final File child : files) {
                                walkFileSystem(child);
                            }
                        }
                    } else {
                        includedFiles.push(file);
                    }
                } catch (final Exception e) {
                    LOGGER.error("Exception walking the file tree : ", e);
                }
            }

        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTerminated(final boolean terminated) {
        this.terminated = terminated;
    }

    /**
     * {@inheritDoc}
     */
    public File getResource() {
        return getResource(5);
    }

    private File getResource(final int retry) {
        File file = null;
        if (retry > 0) {
            if (includedFiles.size() > 0) {
                file = includedFiles.pop();
            } else {
                if (!finished) {
                    LOGGER.info("Waiting for walker : ");
                    THREAD.sleep(10000);
                    file = getResource(retry - 1);
                } else {
                    LOGGER.info("No more files : ");
                }
            }
        }
        return file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResources(final List<File> resources) {
        if (resources == null) {
            return;
        }
        this.includedFiles.addAll(resources);
    }

}