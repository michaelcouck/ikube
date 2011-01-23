package ikube;

import ikube.toolkit.ApplicationContextManager;

/**
 * This is the bootstrap class for stand alone. The libraries that are required are defined in the manifest of the core jar, and the
 * manifest points to the {@code /lib} folder directly under the jar file.
 * 
 * @author Michael Couck
 * @since 23.01.11
 * @version 01.00
 */
public class Ikube {

	public static void main(String[] args) {
		ApplicationContextManager.getApplicationContext();
	}

}
