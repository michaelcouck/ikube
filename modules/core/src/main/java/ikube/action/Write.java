package ikube.action;

import ikube.model.IndexContext;
import ikube.toolkit.ClusterManager;

/**
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Write extends AAction<IndexContext, Boolean> {

	@Override
	public Boolean execute(IndexContext indexContext) {
		try {
			String actionName = getClass().getName();
			if (ClusterManager.anyWorking(actionName, indexContext)) {
				return Boolean.FALSE;
			}
		} finally {
			ClusterManager.setWorking(indexContext, null, Boolean.FALSE);
		}
		return Boolean.TRUE;
	}

}