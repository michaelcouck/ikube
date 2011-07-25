package ikube.integration;

import ikube.IConstants;
import ikube.action.Validator;
import ikube.cluster.IClusterManager;
import ikube.integration.strategy.JspStrategy;
import ikube.integration.strategy.LoadStrategy;
import ikube.listener.ListenerManager;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;
import ikube.toolkit.PropertyConfigurer;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * TODO Document me when I am running properly.
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

	@Test
	public void main() throws Exception {
		if (!isServer()) {
			return;
		}
		waitToFinish();
		validateIndexes();
		validateJsps();
		loadWebService();
	}

	protected void waitToFinish() {
		try {
			Object delay = PropertyConfigurer.getStaticProperty(IConstants.DELAY);
			Thread.sleep(Long.valueOf(delay.toString()) * 2);
			do {
				IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
				boolean anyWorking = clusterManager.anyWorking();
				boolean isWorking = clusterManager.getServer().getWorking();
				logger.info("Any servers working : " + anyWorking + ", this server working : " + isWorking);
				if (!anyWorking && !isWorking) {
					break;
				}
				ListenerManager.removeListeners();
				Thread.sleep(Long.valueOf(delay.toString()) / 2);
			} while (true);
		} catch (Exception e) {
			logger.error("Exception waiting for servers to finish : ", e);
		}
	}

	protected void validateIndexes() {
		@SuppressWarnings("rawtypes")
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext<?> indexContext : indexContexts.values()) {
			new Validator().execute(indexContext);
		}
	}

	protected void validateJsps() throws Exception {
		// Test all the jsps
		new JspStrategy(IConstants.SEP + IConstants.IKUBE, 8080).perform();
	}

	protected void loadWebService() throws Exception {
		// Load test the web service
		new LoadStrategy(10000, 10).perform();
	}

	protected boolean isServer() {
		Properties properties = System.getProperties();
		String osName = System.getProperty("os.name");
		logger.info("Operating system : " + osName + ", server : " + osName.toLowerCase().contains("server") + ", 64 bit : "
				+ properties.getProperty("os.arch").contains("64"));
		if (!osName.toLowerCase().contains("server") && properties.getProperty("os.arch").contains("64")) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

}