package ikube.toolkit.data;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DataGeneratorMedicalTest extends ATest {

	public DataGeneratorMedicalTest() {
		super(DataGeneratorMedicalTest.class);
	}

	@Test
	public void generate() throws Exception {
		EntityManager entityManager = null;
		try {
			entityManager = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_H2).createEntityManager();
			IDataGenerator dataGenerator = new DataGeneratorMedical(entityManager, "doctors.xml", 1);
			dataGenerator.before();
			dataGenerator.generate();
			dataGenerator.after();
			assertTrue(entityManager.createQuery("select from Doctor as d").getResultList().size() > 0);
		} finally {
			if (entityManager != null) {
				try {
					entityManager.close();
				} catch (Exception e) {
					logger.error("Exception closing the entity manager : ", e);
				}
			}
		}
	}

}
