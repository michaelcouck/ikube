package ikube.action;

import ikube.action.rule.AreIndexesCreated;
import ikube.action.rule.DirectoryExistsAndIsLocked;
import ikube.action.rule.IsIndexBackedUp;
import ikube.action.rule.IsIndexCorrupt;
import ikube.action.rule.IsIndexCurrent;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.toolkit.FileUtilities;

import java.io.File;

/**
 * This class will just validate that there are indexes in a searchable condition and if not send a mail to the
 * administrator.
 * 
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Validator extends Action<IndexContext<?>, Boolean> {

	/**
	 * Conditions:<br>
	 * 1) There is an index but it is locked, i.e. an index is running<br>
	 * 2) There are two indexes and one is locked<br>
	 * 3) There is one index, i.e. the current one<br>
	 * 4) There are no indexes<br>
	 * 5) There are indexes but they are corrupt<br>
	 * 6) Any of the above and the searcher is not opened for some reason<br>
	 * <br>
	 * There must be at least one index being generated, or one index created // and one being generated for each index
	 * context
	 */
	@Override
	public Boolean execute(final IndexContext<?> indexContext) {
		String subject = null;
		String body = null;
		boolean everythingInitialized = Boolean.TRUE;
		ikube.model.Action action = start(indexContext.getIndexName(), "");
		try {
			Server server = clusterManager.getServer();
			IsIndexCurrent isIndexCurrent = new IsIndexCurrent();
			IsIndexCorrupt isIndexCorrupt = new IsIndexCorrupt();
			AreIndexesCreated areIndexesCreated = new AreIndexesCreated();
			IsIndexBackedUp isIndexBackedUp = new IsIndexBackedUp();
			String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
			File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexDirectoryPath);
			// Are there any indexes at all
			if (!areIndexesCreated.evaluate(indexContext)) {
				if (latestIndexDirectory == null || !latestIndexDirectory.exists()) {
					subject = "No index : " + indexContext.getIndexName() + ", server : " + server.getAddress();
					body = "No index : " + indexContext.toString();
					everythingInitialized &= Boolean.FALSE;
					sendNotification(subject, body);
				}
			}
			// Is the index corrupt for some reason
			if (isIndexCorrupt.evaluate(indexContext)) {
				subject = "Index corrupt : " + indexContext.getIndexName() + ", server : " + server.getAddress();
				body = "There is an index but it is corrupt. Generally another index will be generated immediately, but "
						+ "if there is a backup for the index the restore will be invoked first, depending on the position of the action "
						+ "in the action set.";
				everythingInitialized &= Boolean.FALSE;
				sendNotification(subject, body);
			}
			// Is the index current
			if (!isIndexCurrent.evaluate(indexContext)) {
				subject = "Index not current : " + indexContext.getIndexName() + ", server : " + server.getAddress();
				body = "The index for " + indexContext.getName() + " is not current. Generally another index "
						+ "wil be generated immediately, this message is just for information.";
				everythingInitialized &= Boolean.FALSE;
				sendNotification(subject, body);
			}
			// Is there an index being generated
			if (latestIndexDirectory != null) {
				DirectoryExistsAndIsLocked directoryExistsAndIsLocked = new DirectoryExistsAndIsLocked();
				File[] serverIndexDirectories = latestIndexDirectory.listFiles();
				for (File serverIndexDirectory : serverIndexDirectories) {
					if (directoryExistsAndIsLocked.evaluate(serverIndexDirectory)) {
						subject = "Index being generated : " + indexContext.getIndexName() + ", server : " + server.getAddress();
						body = "The index is being generated for index context " + indexContext.getName()
								+ ". This message is just for informational purposes, no action is required.";
						everythingInitialized &= Boolean.FALSE;
						sendNotification(subject, body);
						break;
					}
				}
			}
			// Is the index open, and if not why not
			if (indexContext.getIndex().getMultiSearcher() == null) {
				subject = "Index not open : " + indexContext.getIndexName() + ", server : " + server.getAddress();
				body = "Searcher not opened for index " + indexContext.getIndexName()
						+ ". This could require some investigation from the administrator.";
				everythingInitialized &= Boolean.FALSE;
				sendNotification(subject, body);
			}
			// Check that the index is backed up
			if (!isIndexBackedUp.evaluate(indexContext)) {
				subject = "Index not backed up : " + indexContext.getIndexName() + ", server : " + server.getAddress();
				body = "The index is not backed up. Generally this is temporary and the next iteration over the actions "
						+ "will cause the index to be backed up. This can be regarded as an informational message.";
				everythingInitialized &= Boolean.FALSE;
				sendNotification(subject, body);
			}
		} catch (Exception e) {
			logger.error("Exception validating the system : " + indexContext, e);
			everythingInitialized &= Boolean.FALSE;
		} finally {
			stop(action);
		}
		return everythingInitialized;
	}

}