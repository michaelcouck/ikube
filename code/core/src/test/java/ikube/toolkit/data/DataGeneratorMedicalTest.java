package ikube.toolkit.data;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.model.medical.Doctor;

import javax.persistence.EntityManager;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DataGeneratorMedicalTest extends BaseTest {

	public DataGeneratorMedicalTest() {
		super(DataGeneratorMedicalTest.class);
	}

	@Test
	public void generate() throws Exception {
		EntityManager entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
		IDataGenerator dataGenerator = new DataGeneratorMedical(entityManager, "doctors.xml", 1);
		dataGenerator.before();
		dataGenerator.generate();
		assertTrue(entityManager.createNamedQuery(Doctor.FIND_DOCTORS).getResultList().size() > 0);
	}

}
