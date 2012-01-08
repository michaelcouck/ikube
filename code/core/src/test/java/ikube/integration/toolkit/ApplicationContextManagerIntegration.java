package ikube.integration.toolkit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import ikube.IConstants;
import ikube.action.rule.RuleInterceptor;
import ikube.integration.AbstractIntegration;
import ikube.notify.IMailer;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

public class ApplicationContextManagerIntegration extends AbstractIntegration {

	private String	ikubeFolder	= "./" + IConstants.IKUBE;

	@Before
	public void before() {
		FileUtilities.deleteFile(new File(ikubeFolder), 1);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(ikubeFolder), 1);
	}

	@Test
	public void getApplicationContext() {
		ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
		assertNotNull(applicationContext);
		RuleInterceptor ruleInterceptor = ApplicationContextManager.getBean(RuleInterceptor.class);
		assertNotNull(ruleInterceptor);

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