package ikube.datageneration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.ITools;
import ikube.datageneration.model.medical.Address;
import ikube.datageneration.model.medical.Condition;
import ikube.datageneration.model.medical.Medication;
import ikube.datageneration.model.medical.Patient;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for the data generator.
 * 
 * @author Michael Couck
 * @since 14.03.2011
 * @version 01.00
 */
// @Ignore
public class DataGeneratorFourTest {
	
	static {
		Logging.configure();
	}

	private static DataGeneratorFour dataGeneratorFour;
	private static EntityManager entityManager;

	private static Class<?>[] classes = new Class[] { Patient.class, Address.class, Condition.class, Medication.class };
	private String selectFromPatients = "select e from Patient as e";
	private String selectFromAddresses = "select e from Address as e";

	@BeforeClass
	public static void beforeClass() throws Exception {
		ApplicationContextManager.getApplicationContext("/META-INF/spring-h2-jdbc.xml");
		entityManager = Persistence.createEntityManagerFactory(ITools.PERSISTENCE_UNIT_NAME).createEntityManager();
		dataGeneratorFour = new DataGeneratorFour(entityManager, 1, classes);
		dataGeneratorFour.before();
		dataGeneratorFour.delete(entityManager, classes);
	}

	@AfterClass
	public static void afterClass() throws Exception {
		dataGeneratorFour.after();
		entityManager.close();
	}

	@Test
	public void generateFieldData() {
		Address address = dataGeneratorFour.generateFieldData(Address.class, new Address());
		assertNotNull(address.getCountry());
		assertNotNull(address.getNumber());
		assertNotNull(address.getPostCode());
		assertNotNull(address.getPostCode());
		assertNotNull(address.getProvince());
		assertNotNull(address.getStreet());
		Patient patient = dataGeneratorFour.generateFieldData(Patient.class, new Patient());
		assertNull(patient.getAddress());
		assertNotNull(patient.getBirthDate());
		assertNotNull(patient.getDeathDate());
		assertNotNull(patient.getFirstName());
		assertNotNull(patient.getLastName());
		assertNull(patient.getRecords());
	}

	@Test
	public void setTargets() {
		List<Patient> targets = Arrays.asList(new Patient());
		List<Address> results = Arrays.asList(new Address());
		dataGeneratorFour.setTargets(Patient.class, Address.class, targets, results);
		for (Patient patient : targets) {
			assertNotNull(patient.getAddress());
		}
		List<Condition> conditions = Arrays.asList(new Condition());
		List<Medication> medications = Arrays.asList(new Medication());
		dataGeneratorFour.setTargets(Condition.class, Medication.class, conditions, medications);
		dataGeneratorFour.setTargets(Medication.class, Condition.class, medications, conditions);
		for (Condition condition : conditions) {
			assertNotNull(condition.getMedications());
			assertTrue(condition.getMedications().size() > 0);
		}
		for (Medication medication : medications) {
			assertNotNull(medication.getConditions());
			assertTrue(medication.getConditions().size() > 0);
		}
	}

	@Test
	public void persistDelete() throws Exception {
		dataGeneratorFour.persist(entityManager);
		// Verify that there are two entities in the database
		assertEquals(1, entityManager.createQuery(selectFromPatients).getResultList().size());
		assertEquals(1, entityManager.createQuery(selectFromAddresses).getResultList().size());
		dataGeneratorFour.delete(entityManager, Patient.class, Address.class);
		assertEquals(0, entityManager.createQuery(selectFromPatients).getResultList().size());
		assertEquals(0, entityManager.createQuery(selectFromAddresses).getResultList().size());
	}

	@Test
	public void references() throws Exception {
		dataGeneratorFour.persist(entityManager);
		dataGeneratorFour.references(entityManager);
		List<Patient> patients = entityManager.createQuery(selectFromPatients, Patient.class).getResultList();
		for (Patient patient : patients) {
			assertNotNull(patient.getAddress());
		}
	}

	@Test
	public void generate() throws Exception {
		dataGeneratorFour.before();
		dataGeneratorFour.generate();
		dataGeneratorFour.after();
	}

}