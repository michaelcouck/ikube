package ikube;

import ikube.listener.ListenerManager;
import ikube.model.IndexContext;
import ikube.model.faq.Attachment;
import ikube.model.faq.Faq;
import ikube.model.medical.Address;
import ikube.model.medical.Doctor;
import ikube.model.medical.Hospital;
import ikube.model.medical.Inpatient;
import ikube.model.medical.Patient;
import ikube.model.medical.Person;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.data.DataGeneratorFour;
import ikube.toolkit.data.DataGeneratorMedical;
import ikube.toolkit.data.IDataGenerator;

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
	public static void beforeClass() {
		ListenerManager.removeListeners();
		if (INIT) {
			return;
		}
		INIT = Boolean.TRUE;
		ApplicationContextManager.getApplicationContext(IConstants.SPRING_CONFIGURATION_FILE);

		try {
			final EntityManager entityManager = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_H2)
					.createEntityManager();
			PerformanceTester.execute(new PerformanceTester.APerform() {
				@Override
				public void execute() throws Exception {
					try {
						int iterations = 10;
						Class<?>[] classes = new Class[] { Faq.class, Attachment.class, Address.class, Doctor.class, Hospital.class,
								Inpatient.class, Patient.class, Person.class };
						IDataGenerator dataGenerator = new DataGeneratorFour(entityManager, iterations, classes);
						dataGenerator.before();
						dataGenerator.generate();
						dataGenerator.after();

						dataGenerator = new DataGeneratorMedical(entityManager,"doctors.xml", 10);
						dataGenerator.before();
						dataGenerator.generate();
						dataGenerator.after();
					} finally {
						if (entityManager != null) {
							entityManager.close();
						}
					}
				}
			}, "Data generator two insertion : ", 1);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Delete all the old index directories
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}
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

	protected IndexContext indexContext;

	public BaseTest(Class<?> subClass) {
		super(subClass);
		indexContext = ApplicationContextManager.getBean("indexContext");
	}

}