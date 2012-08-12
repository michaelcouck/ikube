package ikube.integration.toolkit;

import static org.junit.Assert.assertNotNull;
import ikube.IConstants;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class ApplicationContextManagerIntegration {

	private String ikubeFolder = "./" + IConstants.IKUBE;

	@After
	public void after() {
		ApplicationContextManager.closeApplicationContext();
		FileUtilities.deleteFile(new File(ikubeFolder), 1);
	}

	@Test
	public void classpath() {
		// First just get the applicaton context from the classpath
		ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
		assertNotNull("The classpath context is the default, if all else fails : ", applicationContext);
	}

	@Test
	public void configuration() {
		// Test with the configuration property set
		File configurationFolder = FileUtilities.findFileRecursively(new File("."), "external");
		File configurationFile = FileUtilities.findFileRecursively(configurationFolder, "spring.xml");
		String configurationFilePath = configurationFile.getAbsolutePath();

		configurationFilePath = FileUtilities.cleanFilePath(configurationFilePath);
		configurationFilePath = "file:" + configurationFilePath;
		System.setProperty(IConstants.IKUBE_CONFIGURATION, configurationFilePath);

		ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext();
		assertNotNull(applicationContext);
	}

	@Test
	public void filesystem() throws Exception {
		File ikubeFolder = FileUtilities.getFile(this.ikubeFolder, Boolean.TRUE);
		File configurationFolder = FileUtilities.findFileRecursively(new File("."), "external");
		FileUtils.copyDirectory(configurationFolder, ikubeFolder);
		ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext();
		assertNotNull(applicationContext);
	}

}