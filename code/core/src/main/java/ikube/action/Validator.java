package ikube.action;

import ikube.action.index.IndexManager;
import ikube.action.rule.AreIndexesCreated;
import ikube.action.rule.DirectoryExistsAndIsLocked;
import ikube.action.rule.IsIndexBackedUp;
import ikube.action.rule.IsIndexCorrupt;
import ikube.action.rule.IsIndexCurrent;
import ikube.model.IndexContext;

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
	 * Conditions:<br>
	 * 1) There is an index but it is locked, i.e. an index is running<br>
	 * 2) There are two indexes and one is locked<br>
	 * 3) There is one index, i.e. the current one<br>
	 * 4) There are no indexes<br>
	 * 5) There are indexes but they are corrupt<br>
	 * 6) Any of the above and the searcher is not opened for some reason<br>
	 * <br>
	 * There must be at least one index being generated, or one index created // and one being generated for each index context
	 */
	@Override
	boolean internalExecute(final IndexContext<?> indexContext) {
		boolean everythingInitialized = Boolean.TRUE;

		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
		// Are there any indexes at all
		if (!new AreIndexesCreated().evaluate(indexContext)) {
			if (latestIndexDirectory == null || !latestIndexDirectory.exists()) {
				String subject = "No index : " + indexContext.getIndexName();
				String body = "No index : " + indexContext.toString();
				everythingInitialized = Boolean.FALSE;
				sendNotification(subject, body);
			}
		}
		// Is the index corrupt for some reason
		if (new IsIndexCorrupt().evaluate(indexContext)) {
			String subject = "Index corrupt : " + indexContext.getIndexName();
			String body = "There is an index but it is corrupt. Generally another index will be generated immediately, but "
					+ "if there is a backup for the index the restore will be invoked first, depending on the position of the action "
					+ "in the action set.";
			everythingInitialized &= Boolean.FALSE;
			sendNotification(subject, body);
		}
		// Is the index current
		if (!new IsIndexCurrent().evaluate(indexContext)) {
			String subject = "Index not current : " + indexContext.getIndexName();
			String body = "The index for " + indexContext.getName() + " is not current. Generally another index "
					+ "wil be generated immediately, this message is just for information.";
			everythingInitialized &= Boolean.FALSE;
			sendNotification(subject, body);
		}
		// Is there an index being generated
		if (latestIndexDirectory != null) {
			DirectoryExistsAndIsLocked directoryExistsAndIsLocked = new DirectoryExistsAndIsLocked();
			File[] serverIndexDirectories = latestIndexDirectory.listFiles();
            if (serverIndexDirectories != null) {
                for (File serverIndexDirectory : serverIndexDirectories) {
                    if (directoryExistsAndIsLocked.evaluate(serverIndexDirectory)) {
                        String subject = "Index being generated : " + indexContext.getIndexName();
                        String body = "The index is being generated for index context " + indexContext.getName()
                            + ". This message is just for informational purposes, no action is required.";
                        everythingInitialized &= Boolean.FALSE;
                        sendNotification(subject, body);
                        break;
                    }
                }
            }
		}
		// Is the index open, and if not why not
		if (indexContext.getMultiSearcher() == null) {
			String subject = "Index not open : " + indexContext.getIndexName();
			String body = "Searcher not opened for index " + indexContext.getIndexName()
					+ ". This could require some investigation from the administrator.";
			everythingInitialized &= Boolean.FALSE;
			sendNotification(subject, body);
		}
		// Check that the index is backed up
		if (!new IsIndexBackedUp().evaluate(indexContext)) {
			String subject = "Index not backed up : " + indexContext.getIndexName();
			String body = "The index is not backed up. Generally this is temporary and the next iteration over the actions "
					+ "will cause the index to be backed up. This can be regarded as an informational message.";
			everythingInitialized &= Boolean.FALSE;
			sendNotification(subject, body);
		}
		return everythingInitialized;
	}

}