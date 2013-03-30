package ikube.action.rule;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;

import java.io.File;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * This action checks to see if the indexes are still ok.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class IsIndexCorrupt extends ARule<IndexContext<?>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
		if (latestIndexDirectory == null) {
			return Boolean.FALSE;
		}
		File[] serverIndexDirectories = latestIndexDirectory.listFiles();
		if (serverIndexDirectories == null) {
			return Boolean.FALSE;
		}
		for (File serverIndexDirectory : serverIndexDirectories) {
			Directory directory = null;
			try {
				directory = FSDirectory.open(serverIndexDirectory);
				if (IndexWriter.isLocked(directory)) {
					continue;
				}
				// If we get here then the directory is the latest, and it is not locked
				// so if it doesn't exist then one of the segment files are deleted or corrupt
				if (!IndexReader.indexExists(directory)) {
					return Boolean.TRUE;
				}
			} catch (Exception e) {
				logger.error("Index possibly corrupt, will try to restore from backup : " + serverIndexDirectory, e);
				// If we have an exception then there is something
				// wrong with one of the server indexes
				return Boolean.TRUE;
			} finally {
				if (directory != null) {
					try {
						directory.close();
					} catch (Exception e) {
						logger.error("Exception closing a possibly corrupt index directory : " + serverIndexDirectory, e);
					}
				}
			}
		}
		return Boolean.FALSE;
	}

}
