package ikube.action;

import ikube.index.IndexManager;
import ikube.model.IndexContext;

import java.io.File;

import org.apache.lucene.index.IndexWriter;

/**
 * @author Michael Couck
 * @since 13.09.2012
 * @version 01.00
 */
public class Optimizer extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean executeInternal(final IndexContext<?> indexContext) {
		ikube.model.Action action = null;
		try {
			action = start(indexContext.getIndexName(), "");
			File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
			logger.info("Latest index directory : " + latestIndexDirectory);
			if (latestIndexDirectory != null && latestIndexDirectory.exists() && latestIndexDirectory.isDirectory()) {
				File[] serverIndexDirectories = latestIndexDirectory.listFiles();
				if (serverIndexDirectories != null && serverIndexDirectories.length > 0) {
					for (File serverIndexDirectory : serverIndexDirectories) {
						IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, serverIndexDirectory, false);
						indexContext.setIndexWriter(indexWriter);
						IndexManager.closeIndexWriter(indexContext);
					}
				}
			}
			return Boolean.TRUE;
		} catch (Exception e) {
			logger.error("Exception trying to optimize the index : ", e);
		} finally {
			stop(action);
		}
		return Boolean.FALSE;
	}

}