package ikube;

import java.io.File;
import java.util.Map;

import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class IntegrationTest extends ATest {
	
	@Before
	public void before() {
		// Delete all the old index directories
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}
	}

	@Test
	// @Ignore
	public void index() throws Exception {
		ApplicationContextManager.getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
		// And we sleep until the index is created
		Thread.sleep(1000 * 60 * 60 * 100);
	}

}
