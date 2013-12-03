package ikube.toolkit;

import static junit.framework.Assert.assertNotNull;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.analytics.WekaClusterer;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * @author Michael Couck
 * @since 01.12.12
 * @version 01.00
 */
public class ApplicationContextManagerTest extends AbstractTest {

	private File springConfig;
	private File externalConfig;
	private String springConfigPath;

	@Before
	public void before() {
		externalConfig = FileUtilities.findDirectoryRecursively(new File("."), "external");
		springConfig = FileUtilities.findFileRecursively(externalConfig, "spring\\.xml");
		springConfigPath = FileUtilities.cleanFilePath(springConfig.getAbsolutePath());
		System.setProperty(IConstants.IKUBE_CONFIGURATION, springConfigPath);
	}

	@Test
	public void getApplicationContext() {
		ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext();
		assertNotNull(applicationContext);
	}

	@Test
	public void setBean() {
		String name = "weka-analyzer";
		ApplicationContextManager.setBean(name, WekaClusterer.class.getName());
		Object wekaAnalyzer = ApplicationContextManager.getBean(name);
		assertNotNull(wekaAnalyzer);
	}
	
	@Test
	public void write() {
		System.out.printf("%03d", 2);
	}

}
