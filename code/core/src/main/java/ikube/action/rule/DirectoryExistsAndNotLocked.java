package ikube.action.rule;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;

/**
 * This rule checks whether the index has been created and is not locked by the index writer. Generally this rule will be used to see if the index searcher can
 * be opened on the index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12.02.2011
 */
public class DirectoryExistsAndNotLocked extends ARule<File> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean evaluate(final File indexDirectory) {
        boolean existsAndNotLocked = Boolean.FALSE;
        Directory directory = null;
        try {
            directory = NIOFSDirectory.open(indexDirectory);
            // directory = FSDirectory.open(indexDirectory);
            // boolean exists = IndexReader.indexExists(directory);
            boolean exists = DirectoryReader.indexExists(directory);
            boolean locked = IndexWriter.isLocked(directory);
            logger.debug("Index exists : " + exists + ", locked : " + locked + ", index directory : " + indexDirectory);
            if (exists && !locked) {
                existsAndNotLocked = Boolean.TRUE;
            }
        } catch (final Exception e) {
            logger.error("Exception checking the directories : ", e);
        } finally {
            try {
                if (directory != null) {
                    directory.close();
                }
            } catch (final Exception e) {
                logger.error("Exception closing the directory : " + directory, e);
            }
        }
        return existsAndNotLocked;
    }

}
