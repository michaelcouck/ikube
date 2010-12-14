package ikube.action;

import ikube.model.IndexContext;
import ikube.model.Url;

/**
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Reset extends Action {

	@Override
	public Boolean execute(IndexContext indexContext) {
		if (getClusterManager().anyWorking()) {
			logger.info("Servers working : ");
			return Boolean.FALSE;
		}
		getClusterManager().clear(Url.class);
		// TODO - clear the server actions
		return Boolean.TRUE;
	}

}