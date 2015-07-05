package ikube.action;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;
import ikube.toolkit.Timer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ikube.action.index.IndexManager.*;

/**
 * This class will find all the 'segments.gen' files in a directory, then open index writers on the directories
 * and close them, essentially optimizing all the indexes recursively in a directory, potentially removing all the
 * lock files, and making the index readable and usable.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 08-02-2013
 */
public class Optimizer extends Action<IndexContext, Boolean> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean internalExecute(final IndexContext indexContext) throws Exception {
        File baseDirectory = new File(indexContext.getIndexDirectoryPath());
        logger.info("Starting at directory : " + baseDirectory);
        List<File> segmentsFiles = FILE.findFilesRecursively(baseDirectory, new ArrayList<File>(), "segments.gen");
        logger.debug("Segments files : " + segmentsFiles);
        for (final File segmentsFile : segmentsFiles) {
            final File indexDirectory = segmentsFile.getParentFile();
            try (Directory directory = NIOFSDirectory.open(indexDirectory)) {
                String[] indexFiles = directory.listAll();
                // We only unlock if we need to optimize this index
                if (indexFiles != null && indexFiles.length > IConstants.MAX_SEGMENTS * 4) {
                    if (!unlockIfLocked(indexContext, directory)) {
                        // Couldn't unlock this index
                        continue;
                    }
                    optimizeIndex(indexContext, indexDirectory, directory);
                }
            }
        }
        return Boolean.TRUE;
    }

    /**
     * This method will close readers on the index(must or the redundant files will not be deleted), optimize
     * the index by opening the index writer, then closing it and forcing Lucene to merge the segments.
     *
     * @param indexContext   the context to optimize the index for
     * @param indexDirectory the index directory that will be optimized
     * @param directory      the Lucene directory that should now be open on the index
     */
    void optimizeIndex(final IndexContext indexContext, final File indexDirectory, final Directory directory) {
        logger.info("Optimizing index : " + indexDirectory.list().length + ", " + indexDirectory);
        logger.debug("Fragments and segments : " + indexDirectory.list().length + ", " + Arrays.toString(indexDirectory.list()));
        class TimedOptimizer implements Timer.Timed {
            @Override
            public void execute() {
                try {
                    THREAD.sleep(10000);
                    IndexWriter indexWriter = openIndexWriter(indexContext, directory, Boolean.FALSE);
                    closeIndexWriter(indexWriter);
                } catch (final Exception e) {
                    logger.error("Exception optimizing index : " + indexDirectory, e);
                }
            }
        }
        Timer.Timed timed = new TimedOptimizer();
        double timeTaken = Timer.execute(timed) / 1000000000 / 60;
        logger.info("Finished optimizing index : " + timeTaken + ", directory :" + indexDirectory);
    }

    /**
     * This method tries to unlock the directory, even if it is a delta index with the writers on the directory,
     * and returns true if the directory is now unlocked, and can proceed to optimize the index, and delete the old
     * index files.
     *
     * @param indexContext the index context to unlock the index directory
     * @param directory    the directory to unlock if necessary
     * @return whether the directory is now unlocked
     * @throws IOException
     */
    boolean unlockIfLocked(final IndexContext indexContext, final Directory directory) throws Exception {
        if (IndexWriter.isLocked(directory)) {
            // Close the readers so we can delete the old files
            if (!new Close().execute(indexContext)) {
                logger.warn("Couldn't close the index reader for optimize : " + indexContext.getName());
            }
            // See if this is a delta index, if so then close the writers first
            if (indexContext.isDelta()) {
                logger.info("Closing index writers for delta index : " + indexContext.getName());
                closeIndexWriters(indexContext);
            }
            IndexWriter.unlock(directory);
            THREAD.sleep(10000);
        }
        if (IndexWriter.isLocked(directory)) {
            // TODO: Should we just delete the lock file here? Dangerous?
            logger.warn("Index locked, can't unlock : " + indexContext.getIndexDirectoryPath());
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}