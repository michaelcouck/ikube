package ikube.action.rule;

import ikube.model.IndexContext;

import java.io.File;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class AreIndexesCreated implements IRule<IndexContext> {

	private boolean expected;

	public boolean evaluate(IndexContext indexContext) {
		File baseIndexDirectory = new File(indexContext.getIndexDirectoryPath() + File.separator + indexContext.getIndexName());
		File[] timeIndexDirectories = baseIndexDirectory.listFiles();
		if (timeIndexDirectories == null) {
			return Boolean.FALSE == expected;
		}
		return Boolean.TRUE == expected;
	}

	@Override
	public void setExpected(boolean expected) {
		this.expected = expected;
	}

}