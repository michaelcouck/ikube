package ikube.integration;

import ikube.logging.Logging;
import ikube.toolkit.ApplicationContextManager;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * This is a test for the 'production' configuration, suitable for a multiple instances.
 * 
 * @author Michael Couck
 * @since 20.12.10
 * @version 01.00
 */
public class Integration {

	static {
		Logging.configure();
	}
	
	private Logger logger = Logger.getLogger(this.getClass());

	@Test
	public void main() throws Exception {
		String osName = System.getProperty("os.name");
		logger.info("Operating system : " + osName);
		if (!osName.toLowerCase().contains("server")) {
			return;
		}
		ApplicationContextManager.getApplicationContext();
		Thread.sleep(1000 * 60 * 60 * 3);
	}

}