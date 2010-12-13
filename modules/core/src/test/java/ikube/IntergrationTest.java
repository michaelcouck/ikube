package ikube;

import ikube.toolkit.ApplicationContextManager;

import org.junit.Test;

public class IntergrationTest extends ATest {

	@Test
	public void index() throws Exception {
		ApplicationContextManager.getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
		// And we sleep until the index is created
		Thread.sleep(1000 * 60 * 60 * 3);
	}

}
