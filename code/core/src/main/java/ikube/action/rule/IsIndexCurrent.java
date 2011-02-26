package ikube.action.rule;

import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;

import java.io.File;

/**
 * Checks to see if the index for the index context is current.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class IsIndexCurrent implements IRule<IndexContext> {

	/**
	 * Checks to see if the current index is not passed it's expiration period. Each index had a parent directory that is a long of the
	 * system time that the index was started. This time signifies the age of the index.
	 * 
	 * @param indexContext
	 *            the index context to check if the index is expired
	 * @return whether the index for this index context is passed it's expiration date
	 */
	public boolean evaluate(IndexContext indexContext) {
		String indexDirectoryPath = indexContext.getIndexDirectoryPath() + File.separator + indexContext.getIndexName();
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexDirectoryPath);
		if (latestIndexDirectory == null) {
			return Boolean.FALSE;
		}
		String indexDirectoryName = latestIndexDirectory.getName();
		long indexDirectoryTime = Long.parseLong(indexDirectoryName);
		long currentTime = System.currentTimeMillis();
		long indexAge = currentTime - indexDirectoryTime;
		if (indexAge > indexContext.getMaxAge()) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

}
