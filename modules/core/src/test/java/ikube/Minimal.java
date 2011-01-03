package ikube;

import ikube.logging.Logging;
import ikube.toolkit.ApplicationContextManager;

/**
 * This is a stand alone using the minimal configuration, as an example.
 * 
 * @author Michael Couck
 * @since 03.01.11
 * @version 01.00
 */
public class Minimal {

	static {
		Logging.configure();
	}

	public static void main(String[] args) throws Exception {
		ApplicationContextManager.getApplicationContext(IConstants.META_INF + IConstants.SEP + "minimal" + IConstants.SEP
				+ IConstants.SPRING_XML);
		Thread.sleep(1000 * 60 * 10);
	}

}
