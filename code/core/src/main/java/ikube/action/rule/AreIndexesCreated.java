package ikube.action.rule;

import ikube.index.IndexManager;
import ikube.model.IndexContext;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * This class checks to see if there are indexes created and that they are all ok, i.e. not corrupt and complete.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class AreIndexesCreated implements IRule<IndexContext> {

	private Logger logger = Logger.getLogger(this.getClass());

	public boolean evaluate(final IndexContext indexContext) {
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File baseIndexDirectory = new File(indexDirectoryPath);
		File[] timeIndexDirectories = baseIndexDirectory.listFiles();
		if (timeIndexDirectories == null || timeIndexDirectories.length == 0) {
			return Boolean.FALSE;
		}
		// Check that there is one index that is in fact created
		for (File timeIndexDirectory : timeIndexDirectories) {
			File[] serverIndexDirectories = timeIndexDirectory.listFiles();
			if (serverIndexDirectories == null || serverIndexDirectories.length == 0) {
				return Boolean.FALSE;
			}
			for (File serverIndexDirectory : serverIndexDirectories) {
				Directory directory = null;
				try {
					directory = FSDirectory.open(serverIndexDirectory);
					if (IndexWriter.isLocked(directory)) {
						continue;
					}
					if (!IndexReader.indexExists(directory)) {
						return Boolean.FALSE;
					}
				} catch (IOException e) {
					logger.error("Exception checking the index directories : ", e);
				} finally {
					if (directory != null) {
						try {
							directory.close();
						} catch (Exception e) {
							logger.error("Exception closing the directory : ", e);
						}
					}
				}
			}
		}
		return Boolean.TRUE;
	}
}