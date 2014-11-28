package ikube.action;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.FILE;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

/**
 * This action cleans old indexes that are corrupt or partially deleted. This class will also
 * delete the files that were unpacked by the indexing of the file system, i.e. the zip and jar
 * files for example.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 31-10-2010
 */
@Deprecated
public class Clean extends Action<IndexContext, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean internalExecute(final IndexContext indexContext) {
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File baseIndexDirectory = FILE.getFile(indexDirectoryPath, Boolean.TRUE);
		File[] timeIndexDirectories = baseIndexDirectory.listFiles();
		if (timeIndexDirectories == null || timeIndexDirectories.length == 0) {
			return Boolean.FALSE;
		}
		// Check all the directories to see if they are partially deleted or if
		// they appear not to be complete or corrupt then delete them
		for (final File timeIndexDirectory : timeIndexDirectories) {
			File[] serverIndexDirectories = timeIndexDirectory.listFiles();
			if (serverIndexDirectories == null || serverIndexDirectories.length == 0) {
				// return Boolean.FALSE;
				continue;
			}
			processDirectories(indexContext, serverIndexDirectories);
		}
		// Try to delete the temporary unzipped files
		FILE.deleteFile(new File(IConstants.TMP_UNZIPPED_FOLDER));
		return Boolean.TRUE;
	}

	private void processDirectories(final IndexContext indexContext, final File... serverIndexDirectories) {
		for (final File serverIndexDirectory : serverIndexDirectories) {
			Directory directory = null;
			try {
				directory = FSDirectory.open(serverIndexDirectory);
				if (IndexWriter.isLocked(directory)) {
					logger.info("Directory still locked : " + serverIndexDirectory);
					continue;
				}
				if (!DirectoryReader.indexExists(directory)) {
                    logger.warn("Deleting directory : " + serverIndexDirectory + ", as it either corrupt, or partially deleted : ");
                    try {
                        new Close().execute(indexContext);
                    } catch (final Exception e) {
                        logger.error("Couldn't close director before delete : " + indexContext.getName(), e);
                    }
                    FILE.deleteFile(serverIndexDirectory);
				}
			} catch (final CorruptIndexException e) {
				logger.error("Index corrupt : " + serverIndexDirectory + ", will try to delete : ", e);
			} catch (final IOException e) {
				logger.error("Directory : " + serverIndexDirectory + " not ok, will try to delete : ", e);
			} catch (final Exception e) {
				logger.error("General exception : " + serverIndexDirectory + " not ok, will try to delete : ", e);
			} finally {
				close(directory);
			}
		}
	}

}