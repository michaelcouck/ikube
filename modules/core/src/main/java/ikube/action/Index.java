package ikube.action;

import ikube.index.IndexManager;
import ikube.index.visitor.IndexableVisitor;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.Server;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.List;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class Index extends Action<IndexContext, Boolean> {

	@Override
	public Boolean execute(IndexContext indexContext) {
		long thread = Thread.currentThread().hashCode();
		try {
			List<Indexable<?>> indexables = indexContext.getIndexables();
			List<IndexableVisitor<Indexable<?>>> indexableVisitors = indexContext.getIndexableVisitors();
			if (indexables == null || indexables.size() == 0 || indexableVisitors == null || indexableVisitors.size() == 0) {
				logger.warn(Logging.getString("No indexables or visitors configured for the context : ", indexContext.getIndexName()));
				return Boolean.FALSE;
			}
			String actionName = getClass().getName();
			boolean indexCurrent = isIndexCurrent(indexContext);
			logger.debug(Logging.getString("Index current : ", indexCurrent, ", ", thread));
			Server server = getClusterManager().getServer();
			if (indexCurrent) {
				// Check if there are any servers still working on this index. Could
				// be that there are more than one 'configurations' defined on this
				// physical machine, meaning that the index is current but that this
				// instance should join the others in in the index
				if (!getClusterManager().areWorking(indexContext.getIndexName(), actionName)) {
					// Nothing to do but go home
					return Boolean.FALSE;
				} else {
					// Check if there is an index for this server in the latest index directory
					File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
					File serverIndexDirectory = new File(latestIndexDirectory, server.getIp());
					File contextIndexDirectory = new File(serverIndexDirectory, indexContext.getName());
					if (contextIndexDirectory.exists()) {
						// If we get here then the index is current and there is
						// an index directory for this server. So we have finished first and there
						// are still other servers working on the index
						return Boolean.FALSE;
					}
				}
			}
			// If we get here then there are two possibilities:
			// 1) The index is not current and we will start the index
			// 2) The index is current and there are other servers working on the index, so we join them
			getClusterManager().setWorking(indexContext, this.getClass().getName(), Boolean.TRUE, System.currentTimeMillis());
			long lastWorkingStartTime = getClusterManager().getLastWorkingTime(indexContext.getIndexName(), actionName);
			if (lastWorkingStartTime <= 0) {
				logger.debug("Other servers working on different actions : ");
				return Boolean.FALSE;
			}
			logger.debug(Logging.getString("Index : Last working time : ", lastWorkingStartTime, ", ", thread));
			// Start the indexing for this server
			IndexManager.openIndexWriter(server.getIp(), indexContext, lastWorkingStartTime);
			try {
				for (IndexableVisitor<Indexable<?>> indexableVisitor : indexableVisitors) {
					for (Indexable<?> indexable : indexables) {
						if (indexableVisitor.getIndexableType().equals(indexable.getClass().getName())) {
							logger.debug(Logging.getString("Visiting indexable : ", indexable, ", ", indexableVisitor));
							indexable.accept(indexableVisitor);
						}
					}
				}
			} catch (Exception e) {
				logger.error("Exception indexing data : " + indexContext.getIndexName(), e);
			}
		} finally {
			IndexManager.closeIndexWriter(indexContext);
			getClusterManager().setWorking(indexContext, null, Boolean.FALSE, 0);
		}
		String indexName = indexContext.getIndexName();
		String contextName = indexContext.getName();
		String message = Logging.getString("Index : Finished indexing : ", indexName, ", ", contextName, ", ", thread);
		logger.debug(message);
		return Boolean.TRUE;
	}

}