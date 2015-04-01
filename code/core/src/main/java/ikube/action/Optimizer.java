package ikube.action;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.FILE;
import ikube.toolkit.Timer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ikube.action.index.IndexManager.closeIndexWriter;
import static ikube.action.index.IndexManager.openIndexWriter;

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
    public boolean internalExecute(final IndexContext indexContext) throws IOException {
        File baseDirectory = new File(indexContext.getIndexDirectoryPath());
        logger.info("Starting at directory : " + baseDirectory);
        List<File> segmentsFiles = FILE.findFilesRecursively(baseDirectory, new ArrayList<File>(), "segments.gen");
        logger.debug("Segments files : " + segmentsFiles);
        for (final File segmentsFile : segmentsFiles) {
            final File indexDirectory = segmentsFile.getParentFile();
            // Can't optimize this index if it is open or being written to
            if (indexContext.getMultiSearcher() != null || (indexContext.getIndexWriters() != null && indexContext.getIndexWriters().length > 0)) {
                logger.info("Index already opened, can't optimize now : " + indexContext.getIndexDirectoryPath());
                continue;
            }
            try (Directory directory = NIOFSDirectory.open(indexDirectory)) {
                if (IndexWriter.isLocked(directory)) {
                    logger.info("Index locked : " + indexContext.getIndexDirectoryPath());
                    continue;
                }
                logger.info("Optimizing index : " + indexDirectory);
                Timer.Timed timed = new Timer.Timed() {
                    @Override
                    public void execute() {
                        try {
                            IndexWriter indexWriter = openIndexWriter(indexContext, directory, Boolean.FALSE);
                            closeIndexWriter(indexWriter);
                        } catch (final Exception e) {
                            logger.error("Exception optimizing index segments file : " + segmentsFile, e);
                        }
                    }
                };
                double timeTaken = Timer.execute(timed) / 1000000000 / 60;
                logger.info("Finished optimizing index : " + timeTaken + ", directory :" + indexDirectory);
            }
        }
        return Boolean.TRUE;
    }

}