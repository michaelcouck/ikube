package ikube;

import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.listener.ListenerManager;
import ikube.listener.Scheduler;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.faq.Attachment;
import ikube.model.faq.Faq;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.data.DataGeneratorFour;
import ikube.toolkit.data.DataGeneratorMedical;
import ikube.toolkit.data.IDataGenerator;

import java.io.File;
import java.io.InputStream;

import javax.persistence.EntityManagerFactory;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;

import com.hazelcast.core.Hazelcast;

/**
 * This is the base class for tests that start up the application context. Generally we don't want to start the context for several reasons
 * not the least of which is that it is time consuming, but in some cases we do. Most tests should be mocked using Mockito and Jmockit.
 * 
 * This class will start the {@link ApplicationContextManager} and initialize the context. This means that the data sources will be started,
 * and injected into Jndi, the aspects will be woven in, Hazelcast will be started, ultimately almost like starting a server, which is nice
 * for integration tests, but not so nice for unit tests that we want to run in a fraction of a second. This startup process can take up to
 * 30 seconds, clearly not nice for a unit test.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class BaseTest extends ATest {

	private static final Logger LOGGER = Logger.getLogger(BaseTest.class);

	protected static ApplicationContext APPLICATION_CONTEXT;
	protected static EntityManagerFactory ENTITY_MANAGER_FACTORY;
	protected static boolean INITIALIZED = Boolean.FALSE;

	@BeforeClass
	public static void beforeClass() {
		shutdownSchedules();
		if (INITIALIZED) {
			return;
		}
		INITIALIZED = Boolean.TRUE;
		// Close down all the instances in the environment because we will be conflicting
		// with each other. Generally there will be none, but the Tomcat shutdown command
		// does not always work
		APPLICATION_CONTEXT = ApplicationContextManager.getApplicationContext();
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		Server server = clusterManager.getServer();
		Hazelcast.getTopic(IConstants.SHUTDOWN_TOPIC).publish(server);
		try {
			// We'll sleep for a while to give the other servers a time to shut down
			Thread.sleep(30000);
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
		shutdownSchedules();
	}

	@AfterClass
	public static void afterClass() {
		shutdownSchedules();
	}

	protected static final void shutdownSchedules() {
		Scheduler.shutdown();
		ListenerManager.removeListeners();
	}

	protected IndexContext<?> indexContext;

	public BaseTest(Class<?> subClass) {
		super(subClass);
		indexContext = ApplicationContextManager.getBean("indexContext");
	}

}