package ikube.action.rule;

import ikube.index.IndexManager;
import ikube.model.IndexContext;

/**
 * This class checks to see if there are indexes created and that they are all ok, i.e. not corrupt and complete.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class AreIndexesCreated extends ARule<IndexContext> {

	public boolean evaluate(final IndexContext indexContext) {
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		return indexesExist(indexDirectoryPath);
	}
}