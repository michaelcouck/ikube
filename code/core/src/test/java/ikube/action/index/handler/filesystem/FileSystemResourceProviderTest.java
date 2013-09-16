package ikube.action.index.handler.filesystem;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import ikube.AbstractTest;
import ikube.model.IndexableFileSystem;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.09.2013
 * @version 01.00
 */
public class FileSystemResourceProviderTest extends AbstractTest {

	private FileSystemResourceProvider fileSystemResourceProvider;

	@Before
	public void before() throws IOException {
		IndexableFileSystem indexableFileSystem = new IndexableFileSystem();
		indexableFileSystem.setPath("./");
		fileSystemResourceProvider = new FileSystemResourceProvider(indexableFileSystem);
	}

	@Test
	public void getResource() {
		File file = fileSystemResourceProvider.getResource();
		assertNotNull(file);
		while (file != null) {
			file = fileSystemResourceProvider.getResource();
			logger.info("Got new file : " + file);
		}
		// All files depleted
		assertNull(file);
	}

}
