package ikube;

import ikube.database.IDataBase;
import ikube.listener.ListenerManager;
import ikube.model.IndexContext;
import ikube.model.faq.Attachment;
import ikube.model.faq.Faq;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.data.DataGeneratorFour;
import ikube.toolkit.data.DataGeneratorMedical;
import ikube.toolkit.data.IDataGenerator;

import java.io.File;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * This is the base class for tests that start up the application context. Generally we don't want to start the context
 * for several reasons not the least of which is that it is time consuming, but in some cases we do. Most tests should
 * be mocked using Mockito and Jmockit.
 * 
 * This class will start the {@link ApplicationContextManager} and initialise the context. This means that the data
 * sources will be started, and injected into Jndi, the aspects will be woven in, Hazelcast will be started, ultimately
 * almost like starting a server, which is nice for integration tests, but not so nice for unit tests that we want to
 * run in a fraction of a second. This startup process can take up to 30 seconds, clearly not nice for a unit test.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class BaseTest extends ATest {

	private static final Logger	LOGGER	= Logger.getLogger(BaseTest.class);

	static {
		Logging.configure();
		ApplicationContextManager.getApplicationContext();
		ListenerManager.getInstance().removeListeners();
		try {
			final int iterations = 0;
			final IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
			PerformanceTester.execute(new PerformanceTester.APerform() {
				@Override
				public void execute() throws Exception {
					Class<?>[] classes = new Class[] { Faq.class, Attachment.class };
					IDataGenerator dataGenerator = new DataGeneratorFour(dataBase, iterations, classes);
					dataGenerator.before();
					dataGenerator.generate();

					File file = FileUtilities.findFileRecursively(new File("."), "doctors.xml");
					InputStream inputStream = file.toURI().toURL().openStream();
					DataGeneratorMedical dataGeneratorMedical = new DataGeneratorMedical(dataBase);
					dataGeneratorMedical.setInputStream(inputStream);
					dataGenerator.before();
					dataGenerator.generate();
				}
			}, "Data generator insertion : ", iterations);
		} catch (Exception e) {
			LOGGER.error("Exception inserting the data for the base test : ", e);
		}
	}

	protected IndexContext<?>	indexContext;

	public BaseTest(Class<?> subClass) {
		super(subClass);
		indexContext = ApplicationContextManager.getBean("indexContext");
	}

}