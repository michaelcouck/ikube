package ikube.action;

import ikube.action.rule.AreIndexesCreated;
import ikube.action.rule.DirectoryExistsAndIsLocked;
import ikube.action.rule.IsIndexCorrupt;
import ikube.action.rule.IsIndexCurrent;
import ikube.cluster.IClusterManager;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;

/**
 * This class will just validate that there are indexes in a searchable condition and if not send a mail to the administrator.
 * 
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Validator extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean execute(final IndexContext<?> indexContext) {
		boolean everythingInitialized = Boolean.TRUE;
		try {
			// Conditions:
			// 1) There is an index but it is locked, i.e. an index is running
			// 2) There are two indexes and one is locked
			// 3) There is one index, i.e. the current one
			// 4) There are no indexes
			// 5) There are indexes but they are corrupt
			// 6) Any of the above and the searcher is not opened for some reason

			// There must be at least one index being generated, or one index created
			// and one being generated for each index context

			Server server = ApplicationContextManager.getBean(IClusterManager.class).getServer();
			AreIndexesCreated areIndexesCreated = new AreIndexesCreated();
			String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
			File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexDirectoryPath);
			if (!areIndexesCreated.evaluate(indexContext)) {
				if (latestIndexDirectory == null || !latestIndexDirectory.exists()) {
					String subject = "No index : " + indexContext.getIndexName() + ", server : " + server.getAddress();
					String body = "No index : " + indexContext.toString();
					sendNotification(subject, body);
					everythingInitialized &= Boolean.FALSE;
					// return Boolean.FALSE;
				}
			}

			IsIndexCorrupt isIndexCorrupt = new IsIndexCorrupt();
			if (isIndexCorrupt.evaluate(indexContext)) {
				String subject = "Index corrupt : " + indexContext.getIndexName() + ", server : " + server.getAddress();
				String body = "There is an index but it is corrupt. Generally another index will be generated immediately, but "
						+ "if there is a backup for the index the restore will be invoked first, depending on the position of the action "
						+ "in the action set.";
				sendNotification(subject, body);
				everythingInitialized &= Boolean.FALSE;
				// return Boolean.FALSE;
			}

			IsIndexCurrent isIndexCurrent = new IsIndexCurrent();
			if (!isIndexCurrent.evaluate(indexContext)) {
				String subject = "Index not current : " + indexContext.getIndexName() + ", server : " + server.getAddress();
				String body = "The index for " + indexContext.getName() + " is not current. Generally another index "
						+ "wil be generated immediately, this message is just for information.";
				sendNotification(subject, body);
				everythingInitialized &= Boolean.FALSE;
				// return Boolean.FALSE;
			}

			// Check to see if there is an index being generated
			if (latestIndexDirectory != null) {
				DirectoryExistsAndIsLocked directoryExistsAndIsLocked = new DirectoryExistsAndIsLocked();
				File[] serverIndexDirectories = latestIndexDirectory.listFiles();
				for (File serverIndexDirectory : serverIndexDirectories) {
					if (directoryExistsAndIsLocked.evaluate(serverIndexDirectory)) {
						String subject = "Index being generated : " + indexContext.getIndexName() + ", server : " + server.getAddress();
						String body = "The index is being generated for index context " + indexContext.getName()
								+ ". This message is just for informational purposes, no action is required.";
						sendNotification(subject, body);
						everythingInitialized &= Boolean.FALSE;
						// return Boolean.FALSE;
					}
				}
			}

			if (indexContext.getIndex().getMultiSearcher() == null) {
				String subject = "Index not open : " + indexContext.getIndexName() + ", server : " + server.getAddress();
				String body = "Searcher not opened for index " + indexContext.getIndexName()
						+ ". This could require some investigation from the administrator.";
				sendNotification(subject, body);
				everythingInitialized &= Boolean.FALSE;
			}

			// return Boolean.TRUE;
		} finally {
			getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getSimpleName(), "", Boolean.FALSE);
		}
		return everythingInitialized;
	}

}