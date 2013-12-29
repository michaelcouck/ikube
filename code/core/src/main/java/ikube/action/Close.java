package ikube.action;

import ikube.model.IndexContext;

import java.io.IOException;

/**
 * This class takes the searcher and tries to close the searcher on the directory.
 * 
 * @author Michael Couck
 * @since 24.08.08
 * @version 01.00
 */
public class Close extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean internalExecute(final IndexContext<?> indexContext) {
		if (indexContext.getMultiSearcher() != null) {
			try {
				indexContext.getMultiSearcher().getIndexReader().close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		indexContext.setMultiSearcher(null);
		return Boolean.TRUE;
	}

}