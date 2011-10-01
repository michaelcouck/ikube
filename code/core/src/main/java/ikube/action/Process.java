package ikube.action;

import ikube.model.IndexContext;

/**
 * This class is for post processing the data, like for example getting all the GeoLocation data from the addresses.
 * 
 * @author Michael Couck
 * @since 05.03.2011
 * @version 01.00
 */
public class Process extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean execute(final IndexContext<?> indexContext) {
		try {
			// TODO Process all the data collected during the indexing
		} finally {
			getClusterManager().stopWorking(indexContext.getIndexName(), this.getClass().getSimpleName(), "");
		}
		return Boolean.TRUE;
	}

}