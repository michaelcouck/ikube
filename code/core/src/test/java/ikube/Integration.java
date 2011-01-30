package ikube;

import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.Map;

import org.junit.Before;

/**
 * This is a test for the 'production' configuration, suitable for a single instance, i.e. no cluster functionality is tested.
 * 
 * @author Michael Couck
 * @since 20.12.10
 * @version 01.00
 */
public class Integration extends ATest {

	@Before
	public void before() {
		// Delete all the old index directories
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}
	}

	public void start() throws Exception {
		ApplicationContextManager.getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
		// And we sleep until the index is created
		Thread.sleep(1000 * 60 * 60 * 3);
	}

	public static void main(String[] args) throws Exception {
		Integration integration = new Integration();
		integration.before();
		integration.start();
	}

}
