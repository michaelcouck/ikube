package ikube.integration;

import ikube.IConstants;
import ikube.integration.strategy.JspStrategy;
import ikube.integration.strategy.LoadStrategy;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 20.12.10
 * @version 01.00
 */
@Ignore
public class Integration {

	static {
		Logging.configure();
	}

	protected Logger	logger	= Logger.getLogger(this.getClass());

	@Test
	public void main() throws Exception {
		if (!isServer()) {
			// return;
		}
		waitToFinish();
		validateJsps();
		loadWebService();
	}

	protected void waitToFinish() {
		try {
			ApplicationContextManager.getApplicationContext();
			Thread.sleep(600000);
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