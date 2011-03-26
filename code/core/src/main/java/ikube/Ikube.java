package ikube;

import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;

/**
 * This is the bootstrap class for stand alone. The libraries that are required are defined in the manifest of the core jar, and the
 * manifest points to the {@code /lib} folder directly under the jar file.
 * 
 * @author Michael Couck
 * @since 23.01.11
 * @version 01.00
 */
public final class Ikube {
	
	private Ikube() {}

	public static void main(final String[] args) {
		Logging.configure();
		ApplicationContextManager.getApplicationContext();
	}

}
