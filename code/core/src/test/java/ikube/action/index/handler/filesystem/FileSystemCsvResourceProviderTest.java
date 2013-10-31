package ikube.action.index.handler.filesystem;

import static junit.framework.Assert.*;

import java.io.File;

import org.junit.Test;

import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;

public class FileSystemCsvResourceProviderTest extends AbstractTest {

	@Test
	public void getResource() {
		File file = FileUtilities.findFileRecursively(new File("."), "languages");
		FileSystemCsvResourceProvider csvResourceProvider = new FileSystemCsvResourceProvider(FileUtilities.cleanFilePath(file.getAbsolutePath()));
		File resource = csvResourceProvider.getResource();
		assertNotNull(resource);
	}

}
