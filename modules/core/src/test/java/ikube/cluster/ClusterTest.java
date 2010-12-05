package ikube.cluster;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.Map;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ClusterTest {

	public static long SLEEP = 3600000;

	@Test
	public void start() throws Exception {
		// Delete all the old index directories
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}
	}

	public static void main(String[] args) throws Exception {
		FileUtilities.deleteFiles(new File("."), IConstants.DATABASE_FILE, IConstants.TRANSACTION_FILES);
		
		// "/cluster/spring.xml"
		String configurationFile = "/META-INF/spring.xml";
		ApplicationContextManager.getApplicationContext(configurationFile);

		ClusterTest clusterTest = new ClusterTest();
		clusterTest.start();
		Thread.sleep(SLEEP);
	}

}