package ikube.action;

import ikube.index.visitor.IndexableVisitor;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.IndexManager;

import java.io.File;
import java.util.List;

public class Index extends AAction<IndexContext, Boolean> {

	@Override
	public Boolean execute(IndexContext indexContext) {
		try {
			List<Indexable<?>> indexables = indexContext.getIndexables();
			List<IndexableVisitor<Indexable<?>>> indexableVisitors = indexContext.getIndexableVisitors();
			if (indexables == null || indexables.size() == 0 || indexableVisitors == null || indexableVisitors.size() == 0) {
				logger.warn("No indexables or visitors configured for the context : " + indexContext.getIndexName());
				return Boolean.FALSE;
			}
			String actionName = getClass().getName();
			boolean indexCurrent = isIndexCurrent(indexContext);
			logger.debug("Index current : " + indexCurrent + ", " + Thread.currentThread().hashCode());
			if (indexCurrent) {
				// Check if there are any servers still working on this index. Could
				// be that there are more than one 'configurations' defined on this
				// physical machine, meaning that the index is current but that this
				// instance should join the others in in the index
				if (!getClusterManager().areWorking(indexContext, actionName)) {
					// Nothing to do but go home
					return Boolean.FALSE;
				} else {
					// Check if there is an index for this server in the latest index directory
					File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
					File serverIndexDirectory = new File(latestIndexDirectory, indexContext.getServerName());
					if (serverIndexDirectory.exists()) {
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
			long lastWorkingStartTime = getClusterManager().getLastWorkingTime(indexContext, actionName);
			if (lastWorkingStartTime <= 0) {
				logger.debug("Other servers working on different actions : ");
				return Boolean.FALSE;
			}
			// getClusterManager().setWorking(indexContext, actionName, Boolean.TRUE, lastWorkingStartTime);
			logger.debug("Index : Last working time : " + lastWorkingStartTime + ", " + Thread.currentThread().hashCode());
			// Start the indexing for this server
			IndexManager.openIndexWriter(indexContext, lastWorkingStartTime);
			try {
				for (IndexableVisitor<Indexable<?>> indexableVisitor : indexableVisitors) {
					for (Indexable<?> indexable : indexables) {
						if (indexableVisitor.getIndexableType().equals(indexable.getClass().getName())) {
							// logger.debug("Visiting indexable : " + indexable + ", " + indexableVisitor);
							indexable.accept(indexableVisitor);
						}
					}
				}
			} catch (Exception e) {
				logger.error("Exception indexing data : " + indexContext.getIndexName(), e);
			}
			IndexManager.closeIndexWriter(indexContext);
		} finally {
			getClusterManager().setWorking(indexContext, null, Boolean.FALSE, 0);
		}
		logger.debug("Index : Finished indexing : " + indexContext.getIndexName() + ", " + indexContext.getServerName() + ", "
				+ Thread.currentThread().hashCode());
		return Boolean.TRUE;
	}

}