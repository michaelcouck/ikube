package ikube.toolkit;

import static org.junit.Assert.assertNotNull;
import ikube.ATest;
import ikube.IConstants;
import ikube.notify.IMailer;
import ikube.notify.Mailer;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class ApplicationContextManagerTest extends ATest {

	public ApplicationContextManagerTest() {
		super(ApplicationContextManagerTest.class);
	}

	@Before
	public void before() {
		ApplicationContextManager.closeApplicationContext();
		FileUtilities.deleteFile(new File("./common"), 1);
		FileUtilities.deleteFile(new File("./" + IConstants.SPRING_XML), 1);
		FileUtilities.deleteFile(new File("./" + IConstants.SPRING_PROPERTIES), 1);
	}

	@After
	public void after() {
		ApplicationContextManager.closeApplicationContext();
		FileUtilities.deleteFile(new File("./common"), 1);
		FileUtilities.deleteFile(new File("./" + IConstants.SPRING_XML), 1);
		FileUtilities.deleteFile(new File("./" + IConstants.SPRING_PROPERTIES), 1);
	}

	@Test
	public void getApplicationContext() {
		Object applicationContext = ApplicationContextManager.getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
		assertNotNull(applicationContext);
	}

	@Test
	public void getApplicationContextExternal() {
		File externalFolder = FileUtilities.findFileRecursively(new File("."), "external");
		FileUtilities.copyFiles(externalFolder, new File("."), "svn");
		ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext();
		assertNotNull("External context should be available : ", applicationContext);
		IMailer mailer = applicationContext.getBean(Mailer.class);
		assertNotNull("The mailer is defined in the beans file : ", mailer);
	}

}