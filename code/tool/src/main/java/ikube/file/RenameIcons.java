package ikube.file;

import java.io.File;

import org.apache.commons.lang.StringUtils;

public class RenameIcons {

	public static void main(String[] args) {
		File iconDirectory = new File("/usr/share/eclipse/workspace/ikube/code/war/src/main/webapp/assets/images/icons/file-types");
		File[] iconFiles = iconDirectory.listFiles();
		for (final File iconFile : iconFiles) {
			String fileName = iconFile.getName();
			fileName = StringUtils.strip(fileName, "1234567890_");
			File renamedIconfFile = new File(iconDirectory, fileName);
			iconFile.renameTo(renamedIconfFile);
		}
	}

}
