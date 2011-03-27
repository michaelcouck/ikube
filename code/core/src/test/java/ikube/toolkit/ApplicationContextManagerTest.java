package ikube.toolkit;

import static org.junit.Assert.assertNotNull;
import ikube.IConstants;

import org.junit.Test;

public class ApplicationContextManagerTest {

	@Test
	public void getApplicationContext() {
		Object applicationContext = ApplicationContextManager.getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
		assertNotNull(applicationContext);
	}

}
