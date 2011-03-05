package ikube;

import ikube.cluster.ClusterIntegration;
import ikube.model.Attachment;
import ikube.model.Faq;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.datageneration.DataGeneratorFour;

import java.io.File;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public abstract class BaseTest extends ATest {

	static {
		ClassLoader.getSystemClassLoader();

		ClusterIntegration.SLEEP = 1000;

		// Delete the database file
		FileUtilities.deleteFiles(new File("."), IConstants.DATABASE_FILE, IConstants.TRANSACTION_FILES, IConstants.DATABASE_FILE);
		ApplicationContextManager.getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_NAME);
		ENTITY_MANAGER = entityManagerFactory.createEntityManager();
		// Delete all the old index directories
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}

		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				Class<?>[] classes = new Class[] { Faq.class, Attachment.class };
				DataGeneratorFour dataGenerator = new DataGeneratorFour(ENTITY_MANAGER, 10, classes);
				dataGenerator.before();
				dataGenerator.delete(ENTITY_MANAGER, classes);
				dataGenerator.generate();
				dataGenerator.after();
			}
		}, "Data generator two insertion : ", 1);
	}

	protected static EntityManager ENTITY_MANAGER;

	protected IndexContext indexContext = ApplicationContextManager.getBean("indexContext");

}