package ikube.action;

import ikube.model.IndexContext;

/**
 * TODO Implement this class. What it needs to do is check the other servers to see if they have generated and index. If
 * so then copy the index from the remote server to this server. Typically this will be when the index directory is
 * defined as ./indexes, i.e. local. In this case each server will index all the data, so the database will be iterated
 * as many times as there are servers. To avoid this when one server is finished just copy the index to this server.
 * 
 * @author Michael Couck
 * @since 18.06.11
 * @version 01.00
 */
public class Copy<E, F> extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean execute(final IndexContext<?> indexContext) {
		try {
			return Boolean.TRUE;
		} finally {
			getClusterManager().stopWorking(getClass().getSimpleName(), indexContext.getIndexName(), "");
		}
	}

}