package ikube.action.index.handler.filesystem;

import static junit.framework.Assert.*;

import java.io.File;

import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;
import org.junit.Test;

import ikube.AbstractTest;

public class FileSystemCsvResourceProviderTest extends AbstractTest {

	@Test
	public void getResource() {
		File file = FILE.findFileRecursively(new File("."), 2, "english.csv").getParentFile();
		FileSystemCsvResourceProvider csvResourceProvider = new FileSystemCsvResourceProvider(FILE.cleanFilePath(file.getAbsolutePath()));
		THREAD.sleep(3000);
		File resource = csvResourceProvider.getResource();
		assertNotNull(resource);
	}

}
