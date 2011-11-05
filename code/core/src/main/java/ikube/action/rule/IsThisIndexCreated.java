package ikube.action.rule;

import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.toolkit.FileUtilities;

import java.io.File;

/**
 * This rule checks to see if this server has created an index for this index context.
 * 
 * @author Michael Couck
 * @since 30.03.2011
 * @version 01.00
 */
public class IsThisIndexCreated extends ARule<IndexContext<?>> {

	/**
	 * @param indexContext
	 *            the index context
	 * @return something whether this server created this index
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		// Check that the timestamp in the servers that are still working is
		// different from the timestamp of the latest index directory for this server
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexDirectoryPath);
		Server server = getClusterManager().getServer();
		String address = server.getAddress();
		// Check that this server has created the index. If so then this server
		// finished first and there are still other servers working, but we should not join them
		if (latestIndexDirectory == null) {
			return Boolean.FALSE;
		}
		File thisIndexDirectory = FileUtilities.findFileRecursively(latestIndexDirectory, address);
		return thisIndexDirectory != null;
	}

}
