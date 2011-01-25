package ikube.cluster;

import ikube.toolkit.ApplicationContextManager;

public class ServerRunner {

	public static void main(String[] args) {
		String configurationFile = args[0];
		ApplicationContextManager.getApplicationContext(configurationFile);
	}

}
