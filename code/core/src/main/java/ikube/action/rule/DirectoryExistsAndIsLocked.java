package ikube.action.rule;

import java.io.File;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

/**
 * This rule checks whether the index exists but is still locked, i.e. still being indexed.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class DirectoryExistsAndIsLocked extends ARule<File> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final File indexDirectory) {
		boolean existsAndIsLocked = Boolean.FALSE;
		Directory directory = null;
		try {
			// directory = FSDirectory.open(indexDirectory);
			directory = NIOFSDirectory.open(indexDirectory);
			boolean exists = IndexReader.indexExists(directory);
			boolean locked = IndexWriter.isLocked(directory);
			if (exists && locked) {
				existsAndIsLocked = Boolean.TRUE;
			}
		} catch (Exception e) {
			logger.error("Exception checking the directories : ", e);
		} finally {
			try {
				if (directory != null) {
					directory.close();
				}
			} catch (Exception e) {
				logger.error("Exception closing the directory : " + directory, e);
			}
		}
		return existsAndIsLocked;
	}

}
