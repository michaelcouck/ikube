package ikube.cluster;

import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

public class ClusterTest {

	public static long SLEEP = 360000000;

	@Test
	@Ignore
	public void start() throws Exception {
		ApplicationContextManager.getApplicationContext(new String[] { "/cluster/spring.xml" });
		// Delete all the old index directories
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}
		Thread.sleep(SLEEP);
	}

	public static void main(String[] args) throws Exception {
		ClusterTest clusterTest = new ClusterTest();
		clusterTest.start();
	}

}