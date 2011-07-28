package ikube.integration;

import ikube.IConstants;
import ikube.integration.strategy.JspStrategy;
import ikube.integration.strategy.LoadStrategy;
import ikube.listener.ListenerManager;
import ikube.model.Server;
import ikube.toolkit.Logging;
import ikube.toolkit.PropertyConfigurer;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

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
		validateJsps();
		loadWebService();
	}

	protected void waitToFinish() {
		try {
			Object delay = PropertyConfigurer.getStaticProperty(IConstants.DELAY);
			long sleep = Long.valueOf(delay.toString()) * 2;
			logger.info("Initial sleep period : " + sleep);
			Thread.sleep(sleep);
			do {
				boolean anyWorking = Boolean.FALSE;
				IMap<String, Server> serversMap = Hazelcast.getMap(Server.class.getName());
				for (Server server : serversMap.values()) {
					if (server.getWorking()) {
						anyWorking = Boolean.TRUE;
						break;
					}
				}
				logger.info("Any servers working : " + anyWorking);
				if (!anyWorking) {
					break;
				}
				ListenerManager.removeListeners();
				Thread.sleep(Long.valueOf(delay.toString()) / 2);
			} while (true);
		} catch (Exception e) {
			logger.error("Exception waiting for servers to finish : ", e);
		}
	}

	protected void validateJsps() throws Exception {
		// Test all the jsps
		new JspStrategy(IConstants.SEP + IConstants.IKUBE, 9080).perform();
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
		return Boolean.FALSE;
	}

}