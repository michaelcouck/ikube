package ikube.toolkit;

import static junit.framework.Assert.assertNotNull;
import ikube.AbstractTest;
import ikube.IConstants;

import java.io.File;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * @author Michael Couck
 * @since 01.12.12
 * @version 01.00
 */
public class ApplicationContextManagerTest extends AbstractTest {

	@Test
	public void getApplicationContext() {
		File externalConfig = FileUtilities.findDirectoryRecursively(new File("."), "external");
		File springConfig = FileUtilities.findFileRecursively(externalConfig, "spring\\.xml");
		String springConfigPath = FileUtilities.cleanFilePath(springConfig.getAbsolutePath());
		System.setProperty(IConstants.IKUBE_CONFIGURATION, springConfigPath);
		ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext();
		assertNotNull(applicationContext);
	}

}
