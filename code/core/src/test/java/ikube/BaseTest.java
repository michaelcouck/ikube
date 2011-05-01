package ikube;

import ikube.listener.ListenerManager;
import ikube.listener.Scheduler;
import ikube.model.IndexContext;
import ikube.model.faq.Attachment;
import ikube.model.faq.Faq;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.data.DataGeneratorFour;
import ikube.toolkit.data.IDataGenerator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class BaseTest extends ATest {

	protected static ApplicationContext APPLICATION_CONTEXT;
	protected static EntityManagerFactory ENTITY_MANAGER_FACTORY;
	protected static boolean initialised = Boolean.FALSE;

	@BeforeClass
	public static void beforeClass() {
		APPLICATION_CONTEXT = ApplicationContextManager.getApplicationContext();
		ListenerManager.removeListeners();
		Scheduler.shutdown();
		if (initialised) {
			return;
		}
		initialised = Boolean.TRUE;
		try {
			ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_H2);
			final EntityManager entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
			final int iterations = 1;
			PerformanceTester.execute(new PerformanceTester.APerform() {
				@Override
				public void execute() throws Exception {
					Class<?>[] classes = new Class[] { Faq.class, Attachment.class };
					IDataGenerator dataGenerator = new DataGeneratorFour(entityManager, iterations, classes);
					dataGenerator.before();
					dataGenerator.generate();
				}
			}, "Data generator insertion : ", iterations);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Scheduler.shutdown();
		ListenerManager.removeListeners();
	}

	@AfterClass
	public static void afterClass() {
		ListenerManager.removeListeners();
	}

	protected IndexContext indexContext;

	public BaseTest(Class<?> subClass) {
		super(subClass);
		indexContext = ApplicationContextManager.getBean("indexContext");
	}

}