package ikube.action.rule;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * This rule checks whether the index has been created and is not locked by the index writer. Generally this rule will be used to see if the
 * index searcher can be opened on the index.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class DirectoryExistsAndNotLocked implements IRule<File> {

	private static final transient Logger LOGGER = Logger.getLogger(DirectoryExistsAndNotLocked.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final File indexDirectory) {
		Directory directory = null;
		try {
			directory = FSDirectory.open(indexDirectory);
			boolean exists = IndexReader.indexExists(directory);
			boolean locked = IndexWriter.isLocked(directory);
			if (exists && !locked) {
				return Boolean.TRUE;
			}
			LOGGER.info("Non existant or locked directory found, will not open on this one yet : " + directory);
		} catch (Exception e) {
			LOGGER.error("Exception checking the directories : ", e);
		} finally {
			try {
				if (directory != null) {
					directory.close();
				}
			} catch (Exception e) {
				LOGGER.error("Exception closing the directory : " + directory, e);
			}
		}
		return Boolean.FALSE;
	}

}
