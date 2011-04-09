package ikube.action;

import ikube.model.IndexContext;

/**
 * TODO Implement me. This class restores an index that has been back up to the index directory designated for indexes.
 * 
 * @author Michael Couck
 * @since 08.04.11
 * @version 01.00
 */
public class Restore extends Action<IndexContext, Boolean> {

	@Override
	public Boolean execute(final IndexContext indexContext) {
		try {
			getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getName(), Boolean.TRUE);
			// See if there is a problems with the index, i.e. if it is corrupt
			// If corrupt or the index contexts are not initialized copy the index to the designated directory for indexes
		} finally {
			getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getName(), Boolean.FALSE);
		}
		return Boolean.TRUE;
	}

}