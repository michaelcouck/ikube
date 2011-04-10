package ikube.action.rule;

import ikube.index.IndexManager;
import ikube.model.IndexContext;

/**
 * This action checks to see if the indexes are still ok.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class IndexesAreCorrupt extends ARule<IndexContext> {

	@Override
	public boolean evaluate(final IndexContext indexContext) {
		String baseIndexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		return indexesExist(baseIndexDirectoryPath);
	}

}
