package ikube.integration.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.integration.AbstractIntegration;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class ApplicationContextManagerIntegration extends AbstractIntegration {

	private String ikubeFolder = "./" + IConstants.IKUBE;

	@After
	public void after() {
		FileUtilities.deleteFile(new File(ikubeFolder), 1);
	}

	@AfterClass
	public static void afterClass() {
		ApplicationContextManager.getApplicationContext();
	}

	@Test
	public void classpath() {
		ApplicationContextManager.closeApplicationContext();
		try {
			// First just get the applicaton context from the classpath
			ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
			assertNotNull("The classpath context is the default, if all else fails : ", applicationContext);
		} finally {
			ApplicationContextManager.closeApplicationContext();
		}
	}

	@Test
	public void configuration() {
		ApplicationContextManager.closeApplicationContext();
		try {
			setExternalConfigurationFile();
			ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext();
			assertNotNull(applicationContext);
			Object mailerExternal = applicationContext.getBean("mailerExternal");
			assertNotNull(mailerExternal);
		} finally {
			System.setProperty(IConstants.IKUBE_CONFIGURATION, "none");
			ApplicationContextManager.closeApplicationContext();
		}
	}

	private void setExternalConfigurationFile() {
		// Test with the configuration property set
		File configurationFolder = FileUtilities.findFileRecursively(new File("."), "external");
		File configurationFile = FileUtilities.findFileRecursively(configurationFolder, "spring.xml");
		String configurationFilePath = configurationFile.getAbsolutePath();

		configurationFilePath = FileUtilities.cleanFilePath(configurationFilePath);
		configurationFilePath = "file:" + configurationFilePath;
		System.setProperty(IConstants.IKUBE_CONFIGURATION, configurationFilePath);
	}

	@Test
	public void filesystem() throws Exception {
		ApplicationContextManager.closeApplicationContext();
		try {
			File ikubeFolder = FileUtilities.getFile(this.ikubeFolder, Boolean.TRUE);
			File configurationFolder = FileUtilities.findFileRecursively(new File("."), "external");
			FileUtils.copyDirectory(configurationFolder, ikubeFolder);
			ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext();
			assertNotNull(applicationContext);
			Object mailerExternal = applicationContext.getBean("mailerExternal");
			assertNotNull(mailerExternal);
		} finally {
			ApplicationContextManager.closeApplicationContext();
		}
	}

	@Test
	@Ignore
	@SuppressWarnings("rawtypes")
	public void setApplicationContext() {
		// What we want to test here is that the index contexts are loaded
		// from the database and put in the application context
		setExternalConfigurationFile();
		
		Mockit.setUpMocks(ApplicationContextManagerMock.class);
		
		
		String configLocation = System.getProperty(IConstants.IKUBE_CONFIGURATION);
		FileSystemXmlApplicationContext fileSystemXmlApplicationContext = new FileSystemXmlApplicationContext(configLocation);
		
		ApplicationContextManager applicationContextManager = new ApplicationContextManager();
		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		IndexContext indexContext = new IndexContext();
		indexContext.setName("indexContextName");
		logger.info("Persisting index context : " + ToStringBuilder.reflectionToString(indexContext, ToStringStyle.SHORT_PREFIX_STYLE));
		dataBase.persist(indexContext);
		Deencapsulation.invoke(applicationContextManager, "registerIndexContexts", fileSystemXmlApplicationContext);
		IndexContext registeredIndexContext = (IndexContext) fileSystemXmlApplicationContext.getBean(indexContext.getName());
		assertNotNull("The bean should have been registered with the application context : ", registeredIndexContext);
		assertEquals("The new index context should be available in the application context : ", indexContext.getName(),
				registeredIndexContext.getName());
	}

}