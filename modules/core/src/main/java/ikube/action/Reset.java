package ikube.action;

import ikube.model.Batch;
import ikube.model.IndexContext;
import ikube.model.Url;

/**
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Reset extends Action<IndexContext, Boolean> {

	@Override
	public Boolean execute(IndexContext indexContext) {
		String actionName = getClass().getName();
		String indexName = indexContext.getIndexName();
		try {
			boolean anyWorking = getClusterManager().anyWorkingOnIndex(indexName);
			logger.debug("Resetting : " + !anyWorking + ", " + indexContext);
			if (anyWorking) {
				return Boolean.FALSE;
			}
			getClusterManager().setWorking(indexName, actionName, null, Boolean.TRUE);
			getClusterManager().clear(Url.class);
			getClusterManager().clear(Batch.class);
			return Boolean.TRUE;
		} finally {
			getClusterManager().setWorking(indexName, null, null, Boolean.FALSE);
		}
	}

}