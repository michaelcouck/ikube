package ikube.action.rule;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class DirectoryExistsAndNotLocked implements IRule<File> {

	private Logger logger = Logger.getLogger(this.getClass());

	public boolean evaluate(File indexDirectory) {
		Directory directory = null;
		try {
			directory = FSDirectory.open(indexDirectory);
			boolean exists = IndexReader.indexExists(directory);
			boolean locked = IndexWriter.isLocked(directory);
			if (exists && !locked) {
				return Boolean.TRUE;
			} else {
				logger.info("Non existant or locked directory found, will not open on this one yet : " + directory);
			}
		} catch (Exception e) {
			logger.error("Exception checking the directories : ", e);
		} finally {
			try {
				directory.close();
			} catch (Exception e) {
				logger.error("Exception closing the directory : " + directory, e);
			}
		}
		return Boolean.FALSE;
	}

}
