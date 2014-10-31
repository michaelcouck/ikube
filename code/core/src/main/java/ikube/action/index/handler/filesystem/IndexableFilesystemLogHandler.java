package ikube.action.index.handler.filesystem;

import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystemLog;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * This handler is a custom handler for the BPost. It will index log files in a particular directory,
 * and unlike the {@link ikube.action.index.handler.filesystem.IndexableFileSystemHandler} which indexes files
 * file by file, this handler will index log files line by line.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 08-02-2011
 */
public class IndexableFilesystemLogHandler extends IndexableHandler<IndexableFileSystemLog> {

    @Autowired
    private LogFileResourceHandler logFileResourceHandler;

    /**
     * {@inheritDoc}
     */
    @Override
    public ForkJoinTask<?> handleIndexableForked(final IndexContext indexContext, final IndexableFileSystemLog indexable) throws Exception {
        IResourceProvider<File> fileSystemResourceProvider = new LogFileResourceProvider(indexable);
        return getRecursiveAction(indexContext, indexable, fileSystemResourceProvider);
    }

    @Override
    protected List<?> handleResource(final IndexContext indexContext, final IndexableFileSystemLog indexableFileSystemLog, final Object resource) {
        logger.info("Handling resource : " + resource + ", thread : " + Thread.currentThread().hashCode());
        try {
            logFileResourceHandler.handleResource(indexContext, indexableFileSystemLog, null, resource);
        } catch (final Exception e) {
            handleException(indexableFileSystemLog, e, "Exception doing log file : " + resource);
        }
        return Collections.EMPTY_LIST;
    }


}