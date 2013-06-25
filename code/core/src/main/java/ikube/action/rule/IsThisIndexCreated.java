package ikube.action.rule;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * This rule checks to see if there is an index, either created or being created.
 * 
 * @author Michael Couck
 * @since 21.06.2013
 * @version 01.00
 */
public class IsThisIndexCreated extends ARule<IndexContext<?>> {

	/**
	 * @param indexContext the index context
	 * @return something whether this server created this index
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		boolean indexCreated = Boolean.TRUE;
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
		if (latestIndexDirectory == null) {
			indexCreated = Boolean.FALSE;
		} else {
			File[] indexDirectories = latestIndexDirectory.listFiles();
			if (indexDirectories == null || indexDirectories.length == 0) {
				indexCreated = Boolean.FALSE;
			} else {
				for (File indexDirectory : indexDirectories) {
					Directory directory = null;
					try {
						directory = FSDirectory.open(indexDirectory);
						boolean exists = IndexReader.indexExists(directory);
						boolean locked = IndexWriter.isLocked(directory);
						indexCreated &= exists || locked;
						// logger.info("Server directory : " + indexDirectory + ", exists : " + exists + ", locked : " + locked + ", created : " + indexCreated);
					} catch (Exception e) {
						logger.error("Exception checking index directory : ", e);
					} finally {
						if (directory != null) {
							try {
								directory.close();
							} catch (IOException e) {
								logger.error("Exception checking index directory : ", e);
							}
						}
					}
				}
			}
		}
		return indexCreated;
	}

}