package ikube.action.rule;

import java.io.File;

/**
 * This rule checks whether the two files are equal, for example there is a new index created but the searcher is open on the index,
 * excluding the new one. In this case there will be a check on the directories that are opened and the directories that are available. If
 * there are directories that are not opened then the searcher should be closed and opened on all the directories.
 * 
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class AreDirectoriesEqual implements IRule<File[]> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final File[] directories) {
		if (directories == null || directories.length != 2 || directories[0] == null || directories[1] == null) {
			return Boolean.FALSE;
		}
		String nameOne = directories[0].getName();
		String nameTwo = directories[1].getName();
		String parentNameOne = directories[0].getParentFile().getName();
		String parentNameTwo = directories[1].getParentFile().getName();
		return (nameOne.equals(nameTwo) && parentNameOne.equals(parentNameTwo));
	}

}
