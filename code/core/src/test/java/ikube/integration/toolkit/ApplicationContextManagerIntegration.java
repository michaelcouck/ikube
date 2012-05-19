package ikube.integration.toolkit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import ikube.IConstants;
import ikube.action.rule.RuleInterceptor;
import ikube.notify.IMailer;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

@Ignore
public class ApplicationContextManagerIntegration {

	private String ikubeFolder = "./" + IConstants.IKUBE;

	@Before
	public void before() {
		FileUtilities.deleteFile(new File(ikubeFolder), 1);
		ApplicationContextManager.closeApplicationContext();
	}

	@After
	public void after() {
		ApplicationContextManager.closeApplicationContext();
		FileUtilities.deleteFile(new File(ikubeFolder), 1);
		System.setProperty(IConstants.IKUBE_CONFIGURATION, null);
	}

	@Test
	public void getApplicationContext() {
		ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
		assertNotNull("External context should be available : ", applicationContext);
		RuleInterceptor ruleInterceptor = ApplicationContextManager.getBean(RuleInterceptor.class);
		assertNotNull(ruleInterceptor);

		Object patientIndex = applicationContext.getBean("patientIndex");
		assertNotNull("The patient index should be available : ", patientIndex);
		try {
			IMailer mailer = (IMailer) applicationContext.getBean("anotherMailerExternalNonExistant");
			assertNull("The mailer is defined in the beans file : ", mailer);
			fail();
		} catch (NoSuchBeanDefinitionException e) {
			// Expected
		}
	}

	@Test
	public void getApplicationContextSpecifiedConfigurationPath() {
		ApplicationContextManager.closeApplicationContext();
		File configurationFolder = FileUtilities.findFileRecursively(new File("."), "external");
		File configurationFile = FileUtilities.findFileRecursively(configurationFolder, "spring.xml");
		String configurationFilePath = configurationFile.getAbsolutePath();

		configurationFilePath = configurationFilePath.replaceAll("/./", "/");
		configurationFilePath = "file:" + configurationFilePath;
		System.setProperty(IConstants.IKUBE_CONFIGURATION, configurationFilePath);

		ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext();
		assertNotNull(applicationContext);
		Object externalMailer = applicationContext.getBean("mailerExternal");
		assertNotNull(externalMailer);
	}

}