package ikube.integration.toolkit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import ikube.ATest;
import ikube.IConstants;
import ikube.action.rule.RuleInterceptor;
import ikube.notify.IMailer;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

public class ApplicationContextManagerTest extends ATest {

	private String	ikubeFolder	= "./" + IConstants.IKUBE;

	public ApplicationContextManagerTest() {
		super(ApplicationContextManagerTest.class);
	}

	@Before
	public void before() {
		ApplicationContextManager.closeApplicationContext();
		FileUtilities.deleteFile(new File(ikubeFolder), 1);
	}

	@After
	public void after() {
		ApplicationContextManager.closeApplicationContext();
		FileUtilities.deleteFile(new File(ikubeFolder), 1);
	}

	@Test
	public void getApplicationContext() {
		Object applicationContext = ApplicationContextManager.getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
		assertNotNull(applicationContext);
		RuleInterceptor ruleInterceptor = ApplicationContextManager.getBean(RuleInterceptor.class);
		assertNotNull(ruleInterceptor);
	}

	@Test
	public void getApplicationContextExternal() {
		File externalFolder = FileUtilities.findFileRecursively(new File("."), "META-INF");
		FileUtilities.copyFiles(externalFolder, new File(ikubeFolder), "svn", "spelling", "flatfile", "persistence");
		ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext();
		assertNotNull("External context should be available : ", applicationContext);
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

}