package ikube.toolkit;

import static org.junit.Assert.assertNotNull;
import ikube.IConstants;

import org.junit.After;
import org.junit.Test;

public class ApplicationContextManagerTest {
	
	@After
	public void after() {
		ApplicationContextManager.closeApplicationContext();
	}

	@Test
	public void getApplicationContext() {
		Object applicationContext = ApplicationContextManager.getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
		assertNotNull(applicationContext);
	}
	
	@Test
	public void getApplicationContextExternal() {
		// TODO Implement this test, we have to see that the ikube folder is
		// added to the classpath and that Spring finds all the files there,including the
		// properties files
	}

}
