package ikube.integration;

import ikube.action.Validator;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * This test runs in its own Jenkins job. Typically it will be triggered by another job and will run along side other similar jobs
 * simulating a cluster. When the job finishes the index generated by the servers is tested.
 * 
 * @author Michael Couck
 * @since 20.12.10
 * @version 01.00
 */
public class Integration {

	static {
		Logging.configure();
	}

	protected Logger logger = Logger.getLogger(this.getClass());
	protected int iterations = 10;

	@Test
	public void main() throws Exception {
		if (!isServer()) {
			return;
		}
		Thread.sleep((long) (Math.random() * 10));
		ApplicationContextManager.getApplicationContext();
		waitToFinish();
		validateIndexes();
	}

	protected void validateIndexes() {
		@SuppressWarnings("rawtypes")
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext<?> indexContext : indexContexts.values()) {
			new Validator().execute(indexContext);
		}
	}

	protected void waitToFinish() {
		try {
			while (true) {
				Thread.sleep(600000);
				IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
				boolean anyWorking = clusterManager.anyWorking();
				boolean isWorking = clusterManager.getServer().getWorking();
				logger.info("Any servers working : " + anyWorking + ", this server working : " + isWorking);
				if (!anyWorking && !isWorking) {
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Exception waiting for servers to finish : ", e);
		}
	}

	protected boolean isServer() {
		Properties properties = System.getProperties();
		String osName = System.getProperty("os.name");
		logger.info("Operating system : " + osName + ", server : " + osName.toLowerCase().contains("server") + ", 64 bit : "
				+ properties.getProperty("os.arch").contains("64"));
		if (!osName.toLowerCase().contains("server") && properties.getProperty("os.arch").contains("64")) {
			return Boolean.FALSE;
		}
		return Boolean.FALSE;
	}

}