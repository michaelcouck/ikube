package ikube.cluster;

import ikube.toolkit.ApplicationContextManager;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class ServerRunner {

	public static void main(final String[] args) {
		String configurationFile = args[0];
		ApplicationContextManager.getApplicationContext(configurationFile);
	}

}
