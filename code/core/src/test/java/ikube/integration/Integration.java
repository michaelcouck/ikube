package ikube.integration;

import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

/**
 * This is a test for the 'production' configuration, suitable for a single instance, i.e. no cluster functionality is tested.
 * 
 * @author Michael Couck
 * @since 20.12.10
 * @version 01.00
 */
public class Integration {

	private Logger logger = Logger.getLogger(this.getClass());

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
	public void main() throws Exception {
		String osName = System.getProperty("os.name");
		logger.info("Operating system : " + osName);
		if (!osName.toLowerCase().contains("server")) {
			return;
		}
		ApplicationContextManager.getApplicationContext();
		Integration integration = new Integration();
		integration.before();
		Thread.sleep(1000 * 60 * 60);
	}

}