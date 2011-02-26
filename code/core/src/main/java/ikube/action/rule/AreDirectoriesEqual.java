package ikube.action.rule;

import java.io.File;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class AreDirectoriesEqual implements IRule<File[]> {

	public boolean evaluate(File[] directories) {
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
