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
		try {
			// Conditions:
			// 1) There is an index but it is locked, i.e. an index is running
			// 2) There are two indexes and one is locked
			// 3) There is one index, i.e. the current one
			// 4) There are no indexes
			// 5) There are indexes but they are corrupt

			// There must be at least one index being generated, or one index created
			// and one being generated for each index context

			Server server = ApplicationContextManager.getBean(IClusterManager.class).getServer();
			AreIndexesCreated areIndexesCreated = new AreIndexesCreated();
			String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
			File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexDirectoryPath);
			if (!areIndexesCreated.evaluate(indexContext)) {
				if (latestIndexDirectory == null || !latestIndexDirectory.exists()) {
					String subject = "1 : No indexes generated for server : " + server.getAddress();
					String body = "No indexes for " + indexContext.getIndexName() + " generated.";
					sendNotification(indexContext, subject, body);
					return Boolean.FALSE;
				}
			}

			IsIndexCorrupt isIndexCorrupt = new IsIndexCorrupt();
			if (isIndexCorrupt.evaluate(indexContext)) {
				String subject = "2 : Index corrupt for server : " + server.getAddress();
				String body = "There is an index but it is corrupt. Generally another index will be generated immediately, but "
						+ "if there is a backup for the index the restore will be invoked first, depending on the position of the action "
						+ "in the action set.";
				sendNotification(indexContext, subject, body);
				return Boolean.FALSE;
			}

			IsIndexCurrent isIndexCurrent = new IsIndexCurrent();
			if (!isIndexCurrent.evaluate(indexContext)) {
				String subject = "3 : Index not current for server : " + server.getAddress();
				String body = "The index for " + indexContext.getName() + " is not current. Generally another index "
						+ "wil be generated immediately, this message is just for information.";
				sendNotification(indexContext, subject, body);
				return Boolean.FALSE;
			}

			// Check to see if there is an index being generated
			DirectoryExistsAndIsLocked directoryExistsAndIsLocked = new DirectoryExistsAndIsLocked();
			File[] serverIndexDirectories = latestIndexDirectory.listFiles();

			for (File serverIndexDirectory : serverIndexDirectories) {
				if (directoryExistsAndIsLocked.evaluate(serverIndexDirectory)) {
					String subject = "4 : Index being generated : " + indexContext.getName();
					String body = "The index is being generated for index context " + indexContext.getName() + ".";
					sendNotification(indexContext, subject, body);
					return Boolean.FALSE;
				}
			}

			return Boolean.TRUE;
		} finally {
			getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getSimpleName(), "", Boolean.FALSE);
		}
	}

}