package ikube.data;

import ikube.model.medical.Doctor;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.Ignore;
import org.junit.Test;

public class DataGeneratorMedicalTest {

	private static final String PERSISTENCE_UNIT_H2 = "IkubePersistenceUnitH2";
	@SuppressWarnings("unused")
	private static final String PERSISTENCE_UNIT_ORACLE = "IkubePersistenceUnitOracle";
	private static final int ITERATIONS = 10;
	private static final Class<?>[] CLASSES = new Class<?>[] { Doctor.class };

	@Test
	@Ignore
	public void generate() throws Exception {
		EntityManager entityManager = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_H2).createEntityManager();
		IDataGenerator dataGenerator = new DataGeneratorMedical(entityManager, ITERATIONS, CLASSES);
		dataGenerator.before();
		dataGenerator.generate();
		dataGenerator.after();
	}

}
