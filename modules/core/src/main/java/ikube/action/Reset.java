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
			String actionName = getClass().getName();
			return getLockManager().resetWorkings(indexContext, actionName);
		} finally {
			getLockManager().setWorking(indexContext, null, Boolean.FALSE, 0);
		}
	}

}
