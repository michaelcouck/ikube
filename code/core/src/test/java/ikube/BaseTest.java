package ikube;

import ikube.datageneration.DataGeneratorFour;
import ikube.datageneration.model.faq.Attachment;
import ikube.datageneration.model.faq.Faq;
import ikube.listener.ListenerManager;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.File;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class BaseTest extends ATest {

	/** We only want the before class to execute once for the whole test batch. */
	private static boolean INIT = Boolean.FALSE;

	@BeforeClass
	public static void beforeClass() throws Exception {
		if (INIT) {
			return;
		}
		INIT = Boolean.TRUE;
		ApplicationContextManager.getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
		// Delete all the old index directories
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}
		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				EntityManager entityManager = null;
				try {
					entityManager = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_NAME).createEntityManager();
					Class<?>[] classes = new Class[] { Faq.class, Attachment.class };
					DataGeneratorFour dataGenerator = new DataGeneratorFour(entityManager, 10, classes);
					dataGenerator.before();
					dataGenerator.generate();
					dataGenerator.after();
				} finally {
					entityManager.close();
				}
			}
		}, "Data generator two insertion : ", 1);
		// Remove all the listeners as they create havoc
		ListenerManager.removeListeners();
	}

	@AfterClass
	public static void afterClass() {
		// Try to delete all the old index files
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File indexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(indexDirectory, 1);
		}
	}

	public BaseTest(Class<?> subClass) {
		super(subClass);
	}

	protected IndexContext indexContext = ApplicationContextManager.getBean("indexContext");

}