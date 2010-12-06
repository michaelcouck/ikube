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
		try {
			boolean anyWorking = getClusterManager().anyWorkingOnIndex(indexContext.getIndexName());
			logger.debug("Resetting : " + !anyWorking + ", " + indexContext);
			if (anyWorking) {
				return Boolean.FALSE;
			}
			getClusterManager().setWorking(indexContext.getIndexName(), getClass().getName(), Boolean.TRUE, System.currentTimeMillis());
			getClusterManager().clear(Url.class);
			getClusterManager().clear(Batch.class);
			return Boolean.TRUE;
		} finally {
			getClusterManager().setWorking(indexContext.getIndexName(), null, Boolean.FALSE, 0);
		}
	}

}