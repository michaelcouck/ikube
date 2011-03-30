package ikube.action.rule;

import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;

/**
 * @author Michael Couck
 * @since 30.03.2011
 * @version 01.00
 */
public class IsThisIndexCreated implements IRule<IndexContext> {

	/**
	 * @param indexContext
	 *            the index context
	 * @return something
	 */
	public boolean evaluate(final IndexContext indexContext) {
		// Check that the timestamp in the servers that are still working is
		// different from the timestamp of the latest index directory for this server
		String indexDirectoryPath = indexContext.getIndexDirectoryPath() + File.separator + indexContext.getIndexName();
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexDirectoryPath);
		Server server = ApplicationContextManager.getBean(IClusterManager.class).getServer();
		String address = server.getAddress();
		// Check that this server has created the index. If so then this server
		// finished first and there are still other servers working, but we should not join them
		if (latestIndexDirectory == null) {
			return Boolean.FALSE;
		}
		File thisIndexDirectory = FileUtilities.findFile(latestIndexDirectory, address);
		return thisIndexDirectory != null;
	}

}
