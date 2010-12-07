package ikube.cluster;

import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;

public class ServerRunner {

	private static Logger LOGGER = Logger.getLogger(ServerRunner.class);

	public static void main(String[] args) {
		String serverAddress = args[0];
		// "/cluster/spring.xml", "/META-INF/spring.xml"
		String configurationFile = "/META-INF/spring.xml";
		ApplicationContextManager.getApplicationContext(configurationFile);
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.getServer().setAddress(serverAddress);

		// Delete all the old index directories
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			indexContext.setIndexDirectoryPath("./" + serverAddress);
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			LOGGER.info("Deleting index directory : " + baseIndexDirectory);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}

	}

}
