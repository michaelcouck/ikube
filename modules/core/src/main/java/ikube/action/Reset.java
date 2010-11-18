package ikube.action;

import ikube.model.IndexContext;

/**
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Reset extends AAction<IndexContext, Boolean> {

	@Override
	public Boolean execute(IndexContext indexContext) {
		try {
			boolean anyWorking = getClusterManager().anyWorkingOnIndex(indexContext);
			getClusterManager().setWorking(indexContext, getClass().getName(), Boolean.TRUE, System.currentTimeMillis());
			if (!anyWorking) {
				logger.debug("Resetting : " + !anyWorking + ", " + indexContext);
				// Reset this index context
				indexContext.setIdNumber(0);
				return Boolean.TRUE;
			} else {
				logger.debug("Not resetting : " + !anyWorking + ", " + indexContext);
			}
			return Boolean.FALSE;
		} finally {
			getClusterManager().setWorking(indexContext, null, Boolean.FALSE, 0);
		}
	}

}
